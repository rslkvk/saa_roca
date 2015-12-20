package dataaccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.sun.tools.hat.internal.model.JavaObject;
import logic.content.ContentConverter;
import logic.content.ContentFilter;
import logic.content.ContentSorter;
import logic.content.ResponseGenerator;
import logic.exception.DBException;
import logic.exception.FilterException;
import logic.validation.ResponseValidation;
import util.RestClient;

import javax.json.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by resul on 08/06/15.
 */
public class CDBContactManagerDAO implements ContactDAO {
    private ObjectMapper objectMapper;
    private RestClient dbConClient;
    private static final String URI = "http://127.0.0.1";
    private static final String PORT = "5984";
    private static final String DB_PATH = "contact-management/";

    public CDBContactManagerDAO() {
        objectMapper = new ObjectMapper();
        dbConClient = new RestClient(URI, PORT, MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public Response createContact(String contact) throws DBException, IOException {
        Response response = null;
        ContentConverter contentConverter = new ContentConverter();
        Map<String, String> dbData = contentConverter.getMapData(contact);
        dbData.put("type", "contact");

        boolean isFavorite = false;
        if(dbData.containsKey("favorite") && dbData.get("favorite").equals("true")) {
            isFavorite = true;
            dbData.remove("favorite");
        }

        contact = contentConverter.getJsonData(dbData);
        response = ((Response) dbConClient.requestPost(DB_PATH, contact));
        validate(response);

        Map<String, String> newContact = contentConverter.getMapData(response.readEntity(String.class));
        dbData.put("_id", newContact.get("id"));
        dbData.put("_rev", newContact.get("_rev"));
        dbData.put("favorite", "true");
        contact = contentConverter.getJsonData(dbData);
        if(isFavorite) {
            setFavorite(contact);
        }

        response = ResponseGenerator.dbReResponse(response, contact);
        return response;
    }

    @Override
    public Response readContact(String contactId) throws DBException, IOException {
        Response response = (Response) dbConClient.requestGet(DB_PATH + contactId);
        String contactEntity;
        String favEntity;
        validate(response);

        Response favResponse = getFavorites();
        favEntity = favResponse.getEntity().toString();
        contactEntity= response.readEntity(String.class);
        contactEntity = contactFavorite(contactEntity, favEntity);
        response = ResponseGenerator.dbReResponse(response, contactEntity);
        return response;
    }

    /**
     * checks
     * @param entity
     * @param favEntity
     * @return
     */
    private String contactFavorite(String entity, String favEntity) {
        JsonObject contact = Json.createReader(new ByteArrayInputStream(entity.getBytes())).readObject();
        JsonReader favReader = Json.createReader(new ByteArrayInputStream(favEntity.getBytes()));

        JsonObject favObject = favReader.readObject();
        String favRev = favObject.getString("_rev");
        JsonArray favorites = favObject.getJsonArray("contacts");

        //favorite document is empty.
        if(favorites.size() == 0) {
            return contact.toString();
        }

        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        String contactId = contact.getString("_id");
        for(int i = 0;  i < favorites.size(); i++) {
            String tmpLink = favorites.getJsonObject(i).getString("_link");
            java.net.URI location = java.net.URI.create(tmpLink);
            String[] path = location.getPath().split("/");
            String tmpId = path[path.length-1];
            for(String key : contact.keySet()) {
                jsonObjectBuilder.add(key, contact.getString(key));
                if(contactId.equals(tmpId)) {
                    jsonObjectBuilder.add("favorite", "true");
                }
                jsonObjectBuilder.add("fav_rev", favRev);
            }
        }
        String returnPart = jsonObjectBuilder.build().toString();
        return returnPart;
    }

    @Override
    public Response updateContact(String contactId, String content, String revision) throws DBException {
        Response response = (Response) dbConClient.request(DB_PATH + contactId, content, "PUT", "rev", revision);
        validate(response);
        return response;
    }

    @Override
    public Response deleteContact(String contact, String ifmatch) throws DBException {

        Response favResponse = ((Response) dbConClient.requestGet(
                DB_PATH + "_design/contact/_view/favorites"));
        validate(favResponse);
        String entity = favResponse.readEntity(String.class);
        JsonObject entityObject = Json.createReader(new ByteArrayInputStream(entity.getBytes())).readObject();

        favResponse = deleteFromFavorite(entityObject, contact);
        validate(favResponse);
        Response response = (Response) dbConClient.request(DB_PATH + contact,
                null, "DELETE", "If-Match",ifmatch);
        validate(response);
        return response;
    }

    @Override
    public Response getContacts() throws DBException, FilterException, IOException {
        Response response = (Response) dbConClient.requestGet(
                DB_PATH + "_design/contact/_view/contacts");
        validate(response);
        JsonArray jsonArray = getContactList(response);
        return ResponseGenerator.dbReResponse(response, jsonArray.toString());
    }

    @Override
    public Response searchContacts(String search) throws DBException {
        Response response = (Response) dbConClient.requestGet(
                DB_PATH + "_design/contact/_view/contacts");
        validate(response);
        String contacts = response.readEntity(String.class);
        JsonReader reader = Json.createReader(new ByteArrayInputStream(contacts.getBytes()));
        JsonObject jobject = reader.readObject();
        JsonArray jarray = jobject.getJsonArray("rows");
        return null;
    }

    @Override
    public Response filterContacts(String filter) throws DBException, FilterException, IOException {
        Response response = (Response) dbConClient.requestGet(
                DB_PATH + "_design/contact/_view/contacts");
        //TODO validate response
        JsonArray jsonArray = getContactList(response);
        String filteredContacts = ContentFilter.filter(jsonArray.toString(), filter);
        System.out.println("Filter contacts: " + filteredContacts);
        return ResponseGenerator.dbReResponse(response, filteredContacts);
    }

    @Override
    public Response sortContacts(String sort) throws DBException, IOException {
        Response response = (Response) dbConClient.requestGet(
                DB_PATH + "_design/contact/_view/contacts");
        JsonArray jsonArray = getContactList(response);
        return ResponseGenerator.dbReResponse(response, ContentSorter.sort(jsonArray, sort));
    }

    @Override
    public Response setFavoriteById(String contactId) throws DBException, IOException {
        Response response = response = ((Response) dbConClient.requestPost(DB_PATH, contactId));
        validate(response);
        String contact = response.readEntity(String.class);
        ContentConverter contentConverter = new ContentConverter();
        Map<String, String> map = contentConverter.getMapData(contact);
        map.put("favorite","true");
        contact = contentConverter.getJsonData(map);
        response = setFavorite(contact);

        return response;
    }

    @Override
    public Response setFavorite(String contact) throws DBException, IOException {
        JsonObject jsonObject = Json.createReader(
                new ByteArrayInputStream(contact.getBytes())).readObject();
        String contactId = jsonObject.getString("_id");

        Response response = null;
        String entity;

        //favdoc exists?
        response = ((Response) dbConClient.requestGet(
                DB_PATH + "_design/contact/_view/favorites"));
        validate(response);
        entity = response.readEntity(String.class);
        JsonObject entityObject = Json.createReader(new ByteArrayInputStream(entity.getBytes())).readObject();
        int rows = entityObject.getInt("total_rows");
        boolean favDocExists = (rows == 0) ? false : true;
        boolean favContactExists = (favDocExists) ? existsInDoc(contactId, entityObject) : false;

        JsonObjectBuilder docBuilder = Json.createObjectBuilder();
        JsonArrayBuilder docArray = Json.createArrayBuilder();

        //add contact to favorite list
        if(jsonObject.keySet().contains("favorite")) {

            jsonObject = getFavoriteContact(jsonObject);

            //if the contact is already in the favorite list.
            if(favContactExists) {
                //nothing
                response = ResponseGenerator.jsonResponse(Response.Status.OK, "{\"title\":\"Contact is your Favorite\"," +
                        "\"message\":\"The contact is already in the favorite list.\"}", "application/json");
                validate(response);
            }

            //if the favorite list exist, but the contact is not in the list, then add.
            else if(favDocExists && !favContactExists) {
                entityObject = entityObject.getJsonArray("rows")
                        .getJsonObject(0).getJsonObject("key");
                JsonArray array = entityObject.getJsonArray("contacts");
                for (int i = 0; i < array.size(); i++) {
                    docArray.add(array.get(i));
                }
                docArray.add(jsonObject);
                docBuilder.add("_id", entityObject.getString("_id"));
                docBuilder.add("_rev", entityObject.getString("_rev"));
                docBuilder.add("type", entityObject.getString("type"));
                docBuilder.add("contacts", docArray);
                response = (Response) dbConClient.request(DB_PATH + entityObject.getString("_id"),
                        docBuilder.build().toString(), "PUT", "rev", entityObject.getString("_rev"));
                validate(response);
//                response = ResponseGenerator.dbReResponse(response, response.readEntity(String.class));
            }
            //if favorite list not exist and the contact is to be add.
            else {
                System.out.println("favorite list is not exist. new doc will be created and contact will be aded.");
                docArray.add(jsonObject);
                docBuilder.add("type", entityObject.getString("type"));
                docBuilder.add("contacts", docArray);
                response = (Response) dbConClient.requestPost(DB_PATH, docBuilder.build().toString());
                validate(response);
//                response = ResponseGenerator.dbReResponse(response, response.readEntity(String.class));
            }

        }
        // if the contact must not be added in the favorite list,
        // or contact will be deleted from the favorite list.
        else if(!jsonObject.keySet().contains("favorite")){
            if(favContactExists) {
                response = deleteFromFavorite(entityObject, jsonObject.getString("_id"));
            }
        }

        return response;
    }

    @Override
    public Response deleteFavorite(String contactId) throws DBException {
        Response response = ((Response) dbConClient.requestGet(
                DB_PATH + "_design/contact/_view/favorites"));
        validate(response);
        String entity = response.readEntity(String.class);
        JsonObject entityObject = Json.createReader(new ByteArrayInputStream(entity.getBytes())).readObject();
        int rows = entityObject.getInt("total_rows");
        boolean favDocExists = (rows == 0) ? false : true;
        boolean favContactExists = (favDocExists) ? existsInDoc(contactId, entityObject) : false;
        if(favContactExists) {
            response = deleteFromFavorite(entityObject, contactId);
        }
        return response;
    }

    private Response deleteFromFavorite(JsonObject favJsonObject, String contactId) throws DBException {
        Response response = null;
        JsonObjectBuilder docBuilder = Json.createObjectBuilder();
        JsonArrayBuilder docArray = Json.createArrayBuilder();

        JsonObject jsonObject = favJsonObject.getJsonArray("rows")
                .getJsonObject(0).getJsonObject("key");
        JsonArray array = jsonObject.getJsonArray("contacts");

        for (int i = 0; i < array.size(); i++) {
            JsonObject tmp = array.getJsonObject(i);
            String tmpId = tmp.getString("_link");
            String[] path = java.net.URI.create(tmpId).getPath().split("/");
            tmpId = path[path.length-1];
            if(!tmpId.equals(contactId)) {
                docArray.add(tmp);
            }
        }
        docBuilder.add("_id", jsonObject.getString("_id"));
        docBuilder.add("_rev", jsonObject.getString("_rev"));
        docBuilder.add("type", jsonObject.getString("type"));
        docBuilder.add("contacts", docArray);
        response = (Response) dbConClient.request(DB_PATH + jsonObject.getString("_id"),
                docBuilder.build().toString(), "PUT", "rev", jsonObject.getString("_rev"));
        validate(response);
        return response;
    }

    @Override
    public Response getFavorites() throws IOException, DBException {
        Response response = (Response) dbConClient.requestGet(
                    DB_PATH + "_design/contact/_view/favorites");
        String entity = response.readEntity(String.class);
        JsonObject jsonObject = Json.createReader(
                new ByteArrayInputStream(entity.getBytes())).readObject();

        JsonObjectBuilder docBuilder = Json.createObjectBuilder();
        JsonArrayBuilder docArray = Json.createArrayBuilder();

        if(jsonObject.getInt("total_rows") == 0) {
            //if no favdoc exists, create new empty favdoc
            docBuilder.add("type", "favorite");
            docBuilder.add("contacts", docArray);
            entity = docBuilder.build().toString();
            response = (Response) dbConClient.requestPost(DB_PATH, entity);
            response = ResponseGenerator.dbReResponse(response, entity);
        } else {
            jsonObject = jsonObject.getJsonArray("rows").getJsonObject(0).getJsonObject("key");
            entity = jsonObject.toString();
            response = ResponseGenerator.dbReResponse(response, entity);
        }
        return response;

    }

    @Override
    public Response createGroup(String group, String contacts) {
        return null;
    }

    @Override
    public Response readGroup(String name) {
        return null;
    }

    @Override
    public Response updateGroup(String group) {
        return null;
    }

    @Override
    public Response deleteGroup(String name) {
        return null;
    }

    @Override
    public Response addContact(String groupName, String contact) {
        return null;
    }

    @Override
    public Response getGroups() {
        return null;
    }

    private boolean existsInDoc(String contactId, JsonObject entityObject) {
        entityObject = entityObject.getJsonArray("rows")
                .getJsonObject(0).getJsonObject("key");
        JsonArray array = entityObject.getJsonArray("contacts");

        for(int i = 0; i < array.size(); i++) {
            JsonObject favContact = array.getJsonObject(i);
            URI location = java.net.URI.create(favContact.getString("_link"));
            String[] path = location.getPath().split("/");
            String favContactId = path[path.length-1];
            if(favContactId.equals(contactId)) {
                return true;
            }
        }
        return false;
    }

    private JsonObject getFavoriteContact(JsonObject jsonObject) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("first_name", jsonObject.getString("first_name"));
        jsonObjectBuilder.add("last_name", jsonObject.getString("last_name"));
        jsonObjectBuilder.add("phone1", jsonObject.getString("phone1"));
        jsonObjectBuilder.add("company_name", jsonObject.getString("company_name"));
        jsonObjectBuilder.add("city", jsonObject.getString("city"));
        jsonObjectBuilder.add("_link", "http://localhost:8080/rest/favorites/"+jsonObject.getString("_id"));
        jsonObject = jsonObjectBuilder.build();
        return jsonObject;
    }

    private JsonArray getContacts(Response response) throws DBException {
        validate(response);
        String contacts = response.readEntity(String.class);
        //read json entity
        JsonReader reader = Json.createReader(new ByteArrayInputStream(contacts.getBytes()));
        JsonObject jobject = reader.readObject();
        JsonArray jarray = jobject.getJsonArray("rows");
        JsonArrayBuilder  jarrBuilder = Json.createArrayBuilder();
        for(int i = 0; i < jarray.size(); i++) {
            jobject = jarray.getJsonObject(i).getJsonObject("key");
            jarrBuilder.add(jobject);
        }
        jarray = jarrBuilder.build();
        System.out.println("JARRAY: " + jarray.toString());
        return jarray;
    }

    private JsonArray getContactList(Response response) throws DBException {
        validate(response);
        String contacts = response.readEntity(String.class);
        //read json entity
        JsonReader reader = Json.createReader(new ByteArrayInputStream(contacts.getBytes()));
        JsonObject jobject = reader.readObject();
        JsonArray jarray = jobject.getJsonArray("rows");
        JsonArrayBuilder  jarrBuilder = Json.createArrayBuilder();
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        for(int i = 0; i < jarray.size(); i++) {
            jobject = jarray.getJsonObject(i).getJsonObject("key");
            jsonObjectBuilder.add("first_name", jobject.getString("first_name"));
            jsonObjectBuilder.add("last_name", jobject.getString("last_name"));
            jsonObjectBuilder.add("company_name", jobject.getString("company_name"));
            jsonObjectBuilder.add("phone1", jobject.getString("phone1"));
            jsonObjectBuilder.add("city", jobject.getString("city"));
            jsonObjectBuilder.add("_link", "http://localhost:8080/rest/contacts/"+jobject.getString("_id"));
            jarrBuilder.add(jsonObjectBuilder.build());
        }
        jarray = jarrBuilder.build();
        return jarray;
    }


    private void validate(Response response) throws DBException {
        String entity;
        if(!statusValid(response.getStatus())) {
            entity = response.readEntity(String.class);
            System.err.print("DB_EXCEPTION: " + response.getStatus() + "..." +
                    entity + "...");
            throw new DBException(entity);
        }
    }

    private boolean statusValid(int status) {
        switch (status) {
            case 200:
                return true;
            case 201:
                return true;
            case 202:
                return true;
            case 204:
                return true;
            case 304:
                return true;
            case 400:
                return false;
            case 401:
                return false;
            case 403:
                return false;
            case 404:
                return false;
            case 405:
                return false;
            case 406:
                return false;
            case 409:
                return false;
            case 412:
                return false;
            case 415:
                return false;
            case 416:
                return false;
            case 417:
                return false;
            case 500:
                return false;
            default:
                return false;
        }
    }
}
