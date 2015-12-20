package logic.content;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;

/**
 * Created by resul on 09/06/15.
 */
public class ResponseGenerator {

    private static final String SERVER_LOCATION="http://localhost:8080/rest/contacts";

    public static Response jsonResponse(Response.Status status, String message, String contentType) {
        String entity;
        entity = "{\""+status.getReasonPhrase()+"\":\""+message+"\"}";
        return getResponse(status, entity, contentType);
    }

    public static Response htmlResponse(Response.Status status, String message, String contentType) {
        String entity = message;

        return getResponse(status, entity, contentType);
    }

    public static Response dbReResponse(Response response, String entity) throws IOException {
        URI location = response.getLocation();
        String[] path;
        int status = response.getStatus();
        if(location != null) {
            path = location.getPath().split("/");
            if(path.length > 0) {
                location = URI.create(SERVER_LOCATION+"/"+path[path.length-1]);
            }
        } else {
            location = URI.create(SERVER_LOCATION);
        }
        //if document is created or updated.
        if(status == 201) {
            status = 303;
        }

        return Response.status(status)
                .type(response.getMediaType())
                .entity(entity)
                .cacheControl(
                        CacheControl.valueOf(
                                response.getHeaderString("Cache-Control")))
                .location(location)
                .build();
    }

    public static Response dbReResponse(Response response, String entity, String resourceLocation) throws IOException {
        URI location = response.getLocation();
        String[] path;
        int status = response.getStatus();

        if(location != null && status != 303) {
            path = location.getPath().split("/");
            if(path.length > 0) {
                location = URI.create(resourceLocation+"/"+path[path.length-1]);
            }
        } else {
            location = URI.create(resourceLocation);
        }
        //if document is created or updated.
        if(status == 201) {
            status = 303;
        }

        return Response.status(status)
                .type(response.getMediaType())
                .entity(entity)
                .cacheControl(
                        CacheControl.valueOf(
                                response.getHeaderString("Cache-Control")))
                .location(location)
                .build();
    }

    public static Response deleteResponse(Response response, String entity, String resourceLocation) {
        URI location = URI.create(resourceLocation);
        int status = response.getStatus();

        //if document is deleted redirect to contact list.
        if(status == 200) {
            status = 303;
        }

        return Response.status(status)
                .type(response.getMediaType())
                .entity(entity)
                .cacheControl(CacheControl.valueOf(
                        response.getHeaderString("Cache-Control")
                )).location(location)
                .build();
    }

    public static Response deleteResponse(Response response, String entity) {
        URI location = URI.create(SERVER_LOCATION);
        int status = response.getStatus();

        //if document is deleted redirect to contact list.
        if(status == 200) {
            status = 303;
        }

        return Response.status(status)
                .type(response.getMediaType())
                .entity(entity)
                .cacheControl(CacheControl.valueOf(
                        response.getHeaderString("Cache-Control")
                )).location(location)
                .build();
    }

    private static Response getResponse(Response.Status status, String entity, String contentType) {
        return Response.status(status).entity(entity).type(contentType).build();
    }

}
