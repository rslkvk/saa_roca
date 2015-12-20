package resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import dataaccess.ContactDAO;
import dataaccess.DAOFactory;
import html.HTMLGen;
import logic.content.ContentConverter;
import logic.content.ResponseGenerator;
import logic.exception.ContactException;
import logic.exception.DBException;
import logic.validation.ContactValidation;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Created by resul on 14/05/15.
 */
@Path("/favorites")
public class Favorite {
    @GET
    @Produces({"text/html;charset=UTF-8", "application/json"})
    public Response getFavorites(@HeaderParam("Accept-Type") String acceptType) {
        ContactDAO contactDAO = DAOFactory.getContactDAO();
        Response response = null;
        String html = "";
        try {
            response = contactDAO.getFavorites();

            if(acceptType == null || !acceptType.equals("application/json")) {
                html = new HTMLGen().getFavoriteList((String) response.getEntity());
                response = ResponseGenerator.htmlResponse(Response.Status.OK, html, "text/html");
            }
        } catch (DBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @GET
    @Path("/{id}")
    @Produces({"text/html;charset=UTF-8", "application/json"})
    public Response getFavorite(@PathParam("id") String contactId,
                                @HeaderParam("Accept-Type") String acceptType,
                                @Context UriInfo uriInfo) {
        Response response = Contact.getContact(uriInfo, acceptType, contactId);
        return response;
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces({"text/html;charset=UTF-8"})
    public static Response createFavorite(MultivaluedMap<String, String> formData) {
        Response response = null;
        try {
            response = Contact.createContact(formData);
            response = ResponseGenerator.dbReResponse(response, response.getEntity().toString(),
                    "http://localhost:8080/rest/favorites");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public static Response createFavorite(String data) {
        Response response = Contact.createContact(data);
        return response;
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html;charset=UTF-8")
    public static Response addToFavorite(@PathParam("id") String contactId,
                                         MultivaluedMap<String, String> data) {
        return Contact.updateContact(contactId, data);
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public static Response addToFavorite(@PathParam("id") String contactId,
                                         String data) {
        ContentConverter contentConverter = new ContentConverter();
        ContactDAO contactDAO = DAOFactory.getContactDAO();
        Map<String, String> map;
        Response response = null;
        try {
            map = contentConverter.getMapData(data);
            ContactValidation.validateContact(map);
            map.put("favorite", "true");
            data = contentConverter.getJsonData(map);
            response = contactDAO.setFavorite(data);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ContactException e) {
            e.printStackTrace();
        } catch (DBException e) {
            e.printStackTrace();
        }
        return response;
    }

    @DELETE
    @Path("/{id}")
    @Produces("application/json")
    public static Response deleteFromFavorite(@PathParam("id") String contactId) {
        ContactDAO contactDAO = DAOFactory.getContactDAO();
        Response response = null;
        try {
            response = contactDAO.deleteFavorite(contactId);
            response = ResponseGenerator.dbReResponse(response, response.getEntity().toString(),
                    "http://localhost:8080/rest/favorites");
        } catch (DBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
