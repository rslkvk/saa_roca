package html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;

/**
 * Created by resul on 31/05/15.
 */
public class HTMLGen {
    private String mainFrame;
    private Document document;

    public HTMLGen() throws IOException {
        File file = new File("/Users/resul/Development/IntelliJIDEA/SAA_ROCA/" +
                "out/artifacts/ROCAServer_war_exploded/"+"layout.html");
        this.document = Jsoup.parse(file, "UTF-8");
        this.mainFrame = document.toString();
    }

    public String getMainFrame() {
        return mainFrame;
    }

    public void setElement(String id, String html) throws IOException {
        Element element = document.body().getElementById(id);
        element.html(html);
        this.mainFrame = document.toString();
    }

    public String getContactList(String jsonContent) throws IOException {
        //1. read template
        Document document = getDocument("contactlist.html");

        //2. build new template with content
        Element contentElement = document.body().getElementById("main-content");
        HtmlOutputContent outputContent = new HtmlOutputContent();

        String htmlContent = outputContent.htmlContactList(jsonContent);
        contentElement.html(htmlContent);

        return document.toString();
    }

    public String getFavoriteList(String jsonContent) throws IOException {
        Document layout = getDocument("layout.html");
        HtmlOutputContent outputContent = new HtmlOutputContent();
        String htmlContent = outputContent.htmlFavoriteList(layout, jsonContent);
        return htmlContent;
    }

    public String getContactDetails(String jsonContent) throws IOException {
        Document document = getDocument("contactdetail.html");
//        Document document = Jsoup.parse(mainFrame);
//        document.getElementById("action-container").remove();
//        document.getElementById("page-state").html("<h1>Contact Details</h1>");
//        document.getElementById("main-content").html()
        HtmlOutputContent outputContent = new HtmlOutputContent();
        return outputContent.htmlContactDetail(document, jsonContent);

    }

    public String getCreateForm() throws IOException {
        Document document = getDocument("contactdetail.html");
        HtmlOutputContent outputContent = new HtmlOutputContent();
        return outputContent.htmlContactCreate(document);
    }

    private Document getDocument(String filename) throws IOException {
        File file = new File("/Users/resul/Development/IntelliJIDEA/SAA_ROCA/" +
                "out/artifacts/ROCAServer_war_exploded/"+filename);
        return Jsoup.parse(file, "UTF-8");
    }

    public String getFavoriteDetails(String json) throws IOException {
        Document document = getDocument("contactdetail.html");
        HtmlOutputContent outputContent = new HtmlOutputContent();
        return outputContent.htmlFavoriteDetails(document, json);
    }
}
