package resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MultivaluedHashMap;

/**
 * Created by resul on 14/05/15.
 */
@Path("/groups")
public class Group {

    @GET
    @Path("/{id}")
    @Produces("text/html")
    public static String getGroup(@PathParam("id") String groupId) {

        return groupId;
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html")
    public static String createGroup(MultivaluedHashMap<String, String> formData) {
        //TODO handle the post request based on form data.
        return formData.toString();
    }

    @PUT
    @Path("/{id}")
    @Produces("text/html")
    public static String addToGroup(@PathParam("id") String groupId, MultivaluedHashMap<String, String> bodyData) {
        return groupId+", " + bodyData.toString();
    }


    @DELETE
    @Path("/{id}")
    @Produces("text/html")
    public static String deleteFromGroup(@PathParam("id") String groupId, MultivaluedHashMap<String, String> bodyData) {

        return "delete contact from group";
    }
}
