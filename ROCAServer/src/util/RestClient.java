package util;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by resul on 30/05/15.
 */
public class RestClient {
    private String url;
    private MediaType acceptType;
    private MediaType contentType;
    private WebTarget webTarget;

    public RestClient(String host, String port, MediaType acceptType, MediaType contentType) {
        this.url = host + ":" + port;
        this.acceptType = acceptType;
        this.contentType = contentType;
        this.webTarget = ClientBuilder.newBuilder().build().target(this.url);
    }

    public void setAcceptType(MediaType acceptType) {
        this.acceptType = acceptType;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    /**
     * returns the rest client configuration
     *
     * @return
     */
    public String getConfig() {
        return "URL: " + this.url + ", \n" +
                "Accept-Type: " + this.acceptType + ", \n" +
                "Content-Type: " + this.contentType + ", \n";
    }

    /**
     * make get request
     *
     * @return
     */
    public Object requestGet() {
        return makeRequest(null, null, "GET");
    }

    public Object requestGet(String urlPath) {
        return makeRequest(urlPath, null, "GET");
    }

    /**
     * make post request with entity body
     *
     * @param content
     * @return
     */
    public Object requestPost(String content) {
        return makeRequest(null, content, "POST");
    }

    public Object requestPost(String urlPath, String content) {
        return makeRequest(urlPath, content, "POST");
    }

    /**
     * make custom request. Null for no content.
     * path=null for no url path
     *
     * @param content
     * @param method
     * @return
     */
    public Object request(String urlPath, String content, String method) {
        return makeRequest(urlPath, content, method);
    }

    public Object request(String urlPath, String content, String method,
                          String optionalHKey, String optionalHValue) {
        return makeRequest(urlPath, optionalHKey, optionalHValue, content, method);
    }

    /**
     * This method builds a web target and makes the desired request
     */
    private Response makeRequest(String urlPath, String content, String methodType) {
        Invocation.Builder builder;
        if (urlPath == null) {
            builder = webTarget.request();
        } else {
            builder = webTarget.path(urlPath).request();
        }
        Response response = builder
                .accept(this.acceptType)
                .build(methodType, Entity.entity(content, this.contentType))
                .invoke();
        return response;
    }

    private Response makeRequest(String urlPath, String optionalHeaderKey, String optionalHeaderValue, String content, String methodType) {
        Invocation.Builder builder;
        if (urlPath == null) {
            builder = webTarget.request().header(optionalHeaderKey, optionalHeaderValue);
        } else {
            builder = webTarget.path(urlPath).request().header(optionalHeaderKey, optionalHeaderValue);
        }
        Response response = builder
                .accept(this.acceptType)
                .build(methodType, Entity.entity(content, this.contentType))
                .invoke();
        return response;
    }
}
