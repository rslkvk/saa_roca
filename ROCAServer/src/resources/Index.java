package resources;

import html.HTMLGen;

import javax.swing.text.html.HTML;
import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by resul on 30/05/15.
 */
@Path("/")
public class Index {

    @GET
    @Produces("text/html"+";charset=UTF-8")
    public String indexPage() {
        HTMLGen htmlGen = null;
        try {
            htmlGen = new HTMLGen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlGen.getMainFrame();
    }

    @GET
    @Produces("text/html"+";charset=UTF-8")
    @Path("/create")
    public String makeHomePage() {
        //TODO create database connection and make first configuration
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

}
