package resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import dataaccess.ContactDAO;
import dataaccess.DAOFactory;
import html.HTMLGen;
import logic.content.ContentConverter;
import logic.content.ResponseGenerator;
import logic.exception.ContactException;
import logic.exception.DBException;
import logic.exception.FilterException;
import logic.validation.ContactValidation;
import util.RestClient;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Created by resul on 05/05/15.
 */
@Path("/contacts")
public class Contact {

    @GET
    @Produces({"text/html;charset=UTF-8", "application/json"})
    public Response getContacts(@Context UriInfo uriInfo, @HeaderParam("Accept-Type") String acceptType) {
        URI requestURI = uriInfo.getAbsolutePath();
        System.out.println(requestURI);
        MultivaluedMap<String, String> pathParams = uriInfo.getQueryParameters();
        //1. ContentConverter
        ContentConverter contentConverter = new ContentConverter();
        //2. DAO
        ContactDAO contactDAO = DAOFactory.getContactDAO();
        Response response = null;
        String html = "";
        try {
            if (pathParams.containsKey("sortby")) {
                response = contactDAO.sortContacts(contentConverter.getJsonData(pathParams));
            } else if (pathParams.containsKey("filterkey")) {
                response = contactDAO.filterContacts(contentConverter.getJsonData(pathParams));
            } else if (pathParams.containsKey("search")) {
                //search
                System.out.println("SEARCH");
            } else {
                response = contactDAO.getContacts();
            }
            //3. HtmlOutputContent
            if(acceptType == null || !acceptType.equals("application/json")) {
                html = new HTMLGen().getContactList((String) response.getEntity());
                response = ResponseGenerator.htmlResponse(Response.Status.OK, html, "text/html");
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (DBException e) {
            response = ResponseGenerator.jsonResponse(Response.Status.FORBIDDEN,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        } catch (FilterException e) {
            response = ResponseGenerator.jsonResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            response = ResponseGenerator.jsonResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        }

        //4. Response
        return response;
    }

    @GET
    @Path("/{id}")
    @Produces({"text/html;charset=UTF-8", "application/json"})
    public static Response getContact(@Context UriInfo uriInfo, @HeaderParam("Accept-Type") String acceptType, @PathParam("id") String contactId) {
        System.out.println("accept-type"+acceptType);
        URI requestURI = uriInfo.getAbsolutePath();
        String[] path = requestURI.getPath().split("/");
        String resource = path[path.length - 2];

        ContactDAO dao = DAOFactory.getContactDAO();
        Response response = null;
        HTMLGen htmlGen;
        String html = "";
        try {
            response = dao.readContact(contactId);
            if(acceptType == null || !acceptType.equals("application/json")) {
                String json = response.getEntity().toString();
                if (resource.equals("contacts")) {
                    html = new HTMLGen().getContactDetails(json);
                } else if (resource.equals("favorites")) {
                    html = new HTMLGen().getFavoriteDetails(json);
                }
                response = ResponseGenerator.htmlResponse(Response.Status.OK, html, "text/html");
            }
        } catch (DBException e) {
            response = ResponseGenerator.jsonResponse(Response.Status.FORBIDDEN,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public static Response createContact(String data) {
        ContentConverter contentConverter = new ContentConverter();
        Map<String, String> map;
        Response response = null;
        try {
            map = contentConverter.getMapData(data);
            ContactValidation.validateContact(map);
            ContactDAO dao = DAOFactory.getContactDAO();
            response = dao.createContact(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DBException e) {
            e.printStackTrace();
        } catch (ContactException e) {
            e.printStackTrace();
        }
        return response;
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html;charset=UTF-8")
    public static Response createContact(MultivaluedMap<String, String> formData) {
        Map<String, String> contactMap;
        Response response = null;
        ContentConverter contentConverter;
        try {
            // validate input contact data
            contentConverter = new ContentConverter();
            contactMap = contentConverter.generateBasicFormMap(formData);
            ContactValidation.validateContact(contactMap);

            if (contentConverter.containsPUT(contactMap)) {
                //Update contact
                return updateContact(contactMap.get("_id"), formData);
            } else if (contentConverter.containsDELETE(contactMap)) {
                return deleteContact(contactMap.get("_rev"), contactMap.get("_id"));
            }

            //remove unused map keys
            contactMap.remove("_method");
            contactMap.remove("_id");
            contactMap.remove("_rev");
//            contactMap.remove("favorite");
            contactMap.remove("groups");

            ContactDAO dao = DAOFactory.getContactDAO();
            response = dao.createContact(
                    contentConverter.getJsonData(contactMap));
//            String entity = response.readEntity(String.class);
//            response = ResponseGenerator.dbReResponse(response, entity);

        } catch (ContactException e) {
            response = ResponseGenerator.jsonResponse(Response.Status.FORBIDDEN,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        } catch (DBException e) {
            response = ResponseGenerator.jsonResponse(Response.Status.FORBIDDEN,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            //TODO internal exception. don't send error message as response.
            response = ResponseGenerator.jsonResponse(Response.Status.FORBIDDEN,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public static Response updateContact(@PathParam("id") String contactId, String data) {
        ContentConverter contentConverter = new ContentConverter();
        ContactDAO contactDAO = DAOFactory.getContactDAO();
        Response response = null;
        try {
            Map<String, String> map = contentConverter.getMapData(data);
            response = contactDAO.updateContact(contactId, data, map.get("_rev"));
            String entity = response.readEntity(String.class);
            response = ResponseGenerator.dbReResponse(response, entity);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DBException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Diese Methode erstellt neuen Kontakt oder ändert einen vorhandenen Kontakt.
     *
     * @param contactURI != null, wenn Daten eines Kontakts geändert werden sollen.
     *                   == null, wenn neuer Kontakt erstellt werden soll.
     * @param data       Daten, die dem Kontakt zugeordnet werden.
     * @return HTML Antwort
     */
    @PUT
    @Path("/{id}")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html;charset=UTF-8")
    public static Response updateContact(@PathParam("id") String contactURI,
                                         MultivaluedMap<String, String> data) {
        ContentConverter contentConverter = new ContentConverter();
        ContactDAO contactDAO = DAOFactory.getContactDAO();
        String jsonData;
        String revision;
        String favorite;
        String group;
        Response response = null;
        Map<String, String> map = contentConverter.generateBasicFormMap(data);

        try {
            ContactValidation.validateContact(map);
            map.remove("_method");
            revision = map.get("_rev");
            favorite = map.get("favorite");
            group = map.remove("groups");

            if (favorite != null && favorite.contains("true")) {
                // contact is to be set as favorite.
                jsonData = contentConverter.getJsonData(map);
                contactDAO.setFavorite(jsonData);
                map.remove("favorite");
            } else {
                map.remove("favorite");
                jsonData = contentConverter.getJsonData(map);
                contactDAO.setFavorite(jsonData);
            }

            jsonData = contentConverter.getJsonData(map);
            response = contactDAO.updateContact(contactURI, jsonData, revision);
            String entity = response.readEntity(String.class);
            response = ResponseGenerator.dbReResponse(response, entity);
        } catch (DBException e) {
            response = ResponseGenerator.htmlResponse(Response.Status.FORBIDDEN,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ContactException e) {
            response = ResponseGenerator.jsonResponse(Response.Status.FORBIDDEN,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        }
        return response;
    }

    @DELETE
    @Path("/{id}")
    @Produces({"text/html", "application/json"})
    public static Response deleteContact(@HeaderParam("If-Match") String ifmatch,
                                         @PathParam("id") String contactId) {
        ContactDAO contactDAO = DAOFactory.getContactDAO();
        Response response = null;
        String entity;
        try {
            response = contactDAO.deleteContact(contactId, ifmatch);
            entity = response.readEntity(String.class);
            response = ResponseGenerator.deleteResponse(response, entity);

        } catch (DBException e) {
            response = ResponseGenerator.htmlResponse(Response.Status.FORBIDDEN,
                    e.getMessage(), MediaType.APPLICATION_JSON);
        }

        return response;
    }

    @GET
    @Path("/create")
    @Produces("text/html")
    public String htmlCreate() {
        HTMLGen htmlGen = null;
        String html = "";
        try {
            htmlGen = new HTMLGen();
            html = htmlGen.getCreateForm();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return html;
    }

    /**
     * temp method to write contacts from file into couchdb
     */
    private void writeFileJson() {
        try {
            Response response;
            JsonReader reader = Json.createReader(new FileInputStream(new File("/Users/resul/Desktop/contacts.json")));
            JsonObject obj = reader.readObject();
            JsonArray arr = obj.getJsonArray("json");
            RestClient cl = new RestClient("http://127.0.0.1", "5984", MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_JSON_TYPE);
            for (int i = 0; i < arr.size(); i++) {
                cl.requestPost("contact-management", arr.get(i).toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}

