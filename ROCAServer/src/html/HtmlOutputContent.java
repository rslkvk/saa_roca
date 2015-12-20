package html;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by resul on 09/06/15.
 */
public class HtmlOutputContent {
    private StringBuilder htmlString;

    public HtmlOutputContent() {
        this.htmlString = new StringBuilder();
    }

    public String htmlContactList(String jsonData) throws IOException {
        Map<String, Map<String, String>> tableMap = new HashMap<String, Map<String, String>>();
        JsonReader reader = Json.createReader(new ByteArrayInputStream(jsonData.getBytes()));
        JsonArray jsonArray = null;
        JsonObject jsonObject = null;
        jsonArray = reader.readArray();

        htmlString.append("<table id=\"contact-list\" class=\"display\">");
        //build table header
        htmlString.append("<thead>");
        htmlString.append("<tr>");
        htmlString.append("<th>");
        htmlString.append("First name");
        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=first_name&value=asc\"><img src=\"\" alt=\"asc\"></a>");
        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=first_name&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("Last name");
        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=last_name&value=asc\"><img src=\"\" alt=\"asc\"></a>");
        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=last_name&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("Phone number");
        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=phone1&value=asc\"><img src=\"\" alt=\"asc\"></a>");
        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=phone1&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("Company Name");
        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=company_name&value=asc\"> <img src=\"\" alt=\"asc\"></a>");
        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=company_name&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("City");
        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=city&value=asc\"><img src=\"\" alt=\"asc\"></a>");
        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=city&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("</th>");
        htmlString.append("</tr>");
        htmlString.append("</thead>");
        //build table body
        htmlString.append("<tbody>");
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject = jsonArray.getJsonObject(i);
            jsonObjectToHtml(jsonObject);
        }
        htmlString.append("</tbody>");
        htmlString.append("</table>");

//        String table = mapToHTMLTable(tableMap);

        return this.htmlString.toString();
    }

    private void jsonObjectToHtml(JsonObject jsonObject) {
        htmlString.append("<tr>");
        htmlString.append("<td>");
        htmlString.append(jsonObject.getString("first_name"));
        htmlString.append("</td>");
        htmlString.append("<td>");
        htmlString.append(jsonObject.getString("last_name"));
        htmlString.append("</td>");
        htmlString.append("<td>");
        htmlString.append(jsonObject.getString("phone1"));
        htmlString.append("</td>");
        htmlString.append("<td>");
        htmlString.append(jsonObject.getString("company_name"));
        htmlString.append("</td>");
        htmlString.append("<td>");
        htmlString.append(jsonObject.getString("city"));
        htmlString.append("</td>");
        htmlString.append("<td>");
        htmlString.append("<a id=\"more\" href=\"" + jsonObject.getString("_link") + "\"> <img src=\"\" alt=\"more\"</a>");
        htmlString.append("</td>");
        htmlString.append("</tr>");
    }

    public String htmlContactDetail(Document document, String json) {
        Document doc = document;
        Element element = doc.body().getElementById("main-content")
                .getElementById("detail");


        JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(json.getBytes()));
        JsonObject jsonObject = jsonReader.readObject();

//        element.attr("action", "/rest/contacts/" + jsonObject.getString("_id"));

        //Formular
        for(String key : jsonObject.keySet()) {
            Element elem = element.getElementById(key);
            if(elem != null) {
                elem.getElementsByTag("input")
                  .attr("value", jsonObject.getString(key));
            }
            // if contact is already favorite, then favorite input to be selected.
            if(key.equals("favorite")) {
                System.out.println("fav_rev" + jsonObject.getString("fav_rev"));
                elem.getElementsByTag("input").remove();
                elem.html("<label>Favorite</label><input type=\"checkbox\" name=\"favorite\" value=\"true\" checked>");
            } else if(key.equals("groups")) {

            }
        }

        //Delete Button
        element = doc.body().getElementById("main-content")
                .getElementById("delete_button");
        element.getElementById("id").getElementsByTag("input")
                .attr("value", jsonObject.getString("_id"));
        element.getElementById("rev").getElementsByTag("input")
                .attr("value", jsonObject.getString("_rev"));

        return doc.toString();
    }

    public String htmlContactCreate(Document document) {
        Document doc = document;
        //change page title
        doc.body().getElementById("page-state").html("<h1>Create new Contact</h1>");

        Element element = doc.body().getElementById("main-content");
        //change method
        element.getElementById("detail").getElementById("_method")
                .getElementsByTag("input").attr("value", "POST");

        //rename save button
        element.getElementById("detail").getElementById("savebutton").attr("value", "Create");

        //remove the Delete button
        element.getElementById("delete_button").remove();

        return doc.toString();
    }

    public String htmlFavoriteList(Document document, String jsonContent) {
        Document doc = document;


        JsonObject jsonObject = Json.createReader(new ByteArrayInputStream(jsonContent.getBytes())).readObject();
        JsonArray contacts = jsonObject.getJsonArray("contacts");

        htmlString.append("<table id=\"contact-list\" class=\"display\">");
        //build table header
        htmlString.append("<thead>");
        htmlString.append("<tr>");
        htmlString.append("<th>");
        htmlString.append("First name");
//        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=first_name&value=asc\"><img src=\"\" alt=\"asc\"></a>");
//        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=first_name&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("Last name");
//        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=last_name&value=asc\"><img src=\"\" alt=\"asc\"></a>");
//        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=last_name&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("Phone number");
//        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=phone1&value=asc\"><img src=\"\" alt=\"asc\"></a>");
//        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=phone1&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("Company Name");
//        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=company_name&value=asc\"> <img src=\"\" alt=\"asc\"></a>");
//        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=company_name&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("City");
//        htmlString.append(" <a class=\"up\" href=\"http://localhost:8080/rest/contacts?sortby=city&value=asc\"><img src=\"\" alt=\"asc\"></a>");
//        htmlString.append(" <a class=\"down\" href=\"http://localhost:8080/rest/contacts?sortby=city&value=desc\"><img src=\"\" alt=\"desc\"></a>");
        htmlString.append("</th>");
        htmlString.append("<th>");
        htmlString.append("</th>");
        htmlString.append("</tr>");
        htmlString.append("</thead>");
        //build table body
        htmlString.append("<tbody>");
        for (int i = 0; i < contacts.size(); i++) {
            jsonObject = contacts.getJsonObject(i);
            jsonObjectToHtml(jsonObject);
        }
        htmlString.append("</tbody>");
        htmlString.append("</table>");


        String actionForm =
            "<form class=\"action-forms\" action=\"/rest/contacts\" method=\"get\">"+
                "<label>Show contact list</label>"+
                "<input class=\"button\" type=\"submit\" value=\"contacts\">" +
            "</form>" +
            "<form class=\"action-forms\" action=\"/rest/groups\" method=\"get\">" +
            "<label>Show groups </label>" +
            "<input class=\"button\" type=\"submit\" value=\"groups\">" +
            "</form>" +
            "<!--<form class=\"action-forms\" action=\"/rest/favorites\" method=\"get\">" +
                "<label >Show favorite list</label>" +
                "<input class=\"button\" type=\"submit\" value=\"favorites\">" +
            "</form>-->";

        doc.body().getElementById("action-container").html(actionForm);
        doc.body().getElementById("page-state").html("<h1>Favorite List<h1>");
        doc.body().getElementById("main-content").html(htmlString.toString());



        return doc.toString();
    }

    public String htmlFavoriteDetails(Document document, String json) {
        Document doc = document;

        doc.body().getElementById("page-state").html("<h1>Favorite Contact Details<h1>");
        Element element = doc.body().getElementById("main-content")
                .getElementById("detail");

        element.attr("action", "/rest/favorites/");

        JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(json.getBytes()));
        JsonObject jsonObject = jsonReader.readObject();

//        element.attr("action", "/rest/contacts/" + jsonObject.getString("_id"));

        //Formular
        for(String key : jsonObject.keySet()) {
            Element elem = element.getElementById(key);
            if(elem != null) {
                elem.getElementsByTag("input")
                        .attr("value", jsonObject.getString(key));
            }
            // if contact is already favorite, then favorite input to be selected.
            if(key.equals("favorite")) {
                System.out.println("fav_rev" + jsonObject.getString("fav_rev"));
                elem.getElementsByTag("input").remove();
                elem.html("<label>Favorite</label><input type=\"checkbox\" name=\"favorite\" value=\"true\" checked>");
            } else if(key.equals("groups")) {

            }
        }

        //Delete Button
        element = doc.body().getElementById("main-content")
                .getElementById("delete_button");
        element.attr("action","/rest/favorites/");
        element.getElementById("id").getElementsByTag("input")
                .attr("value", jsonObject.getString("_id"));
        element.getElementById("rev").getElementsByTag("input")
                .attr("value", jsonObject.getString("_rev"));


        //Cancel Button
        element = doc.body().getElementById("main-content")
                .getElementById("cancel_button");
        element.attr("action", "/rest/favorites");

        return doc.toString();
    }
}
