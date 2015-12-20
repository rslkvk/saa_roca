package logic.content;

import logic.exception.ContactException;
import logic.exception.FilterException;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;

/**
 * Created by resul on 14/06/15.
 */
public class ContentFilter {
    private static String entity;
    private ContentFilter() {}

    public static String filter(String entity, String criteria) throws FilterException {
        JsonReader reader = Json.createReader(
                new ByteArrayInputStream(criteria.getBytes()));
        JsonObject jobj = reader.readObject();
        String key = jobj.getString("filterkey");
        System.out.println("key: " + key);
        String filter = jobj.getString("filter");
        String value = jobj.getString("value");
        switch (filter) {
            case "begins_with":
                System.out.println("key: " + key + "filter: " + filter + "value: " + value);
                return begins_with(entity, key, value);
            case "ends_with":
                return ends_with(entity, key, value);
            case "contains":
                return contains(entity, key, value);
            default:
                if(key.contains("group")) {
                    group(entity, value);
                }
                break;
        }
        throw new FilterException("Cannot find any contact with this filter parameters. " +
                "Please check your filter and try again.");
    }

    private static String begins_with(String entity, String key, String value) {
        Pattern p = Pattern.compile("(("+value+")(\\s*|\\w*|\\W*)+)");
        return matchPattern(p, entity, key);
    }


    private static String ends_with(String entity, String key, String value) {
        Pattern p = Pattern.compile("((\\w*|\\W*)+("+value+"))");
        return matchPattern(p, entity, key);
    }

    private static String contains(String entity, String key, String value) {
        Pattern p = Pattern.compile("((\\w|\\W|\\-?)*("+value+")(\\w|\\W|\\-?)*)");
        return matchPattern(p, entity, key);
    }

    private static void group(String entity, String value) {
        //TODO filter group
    }

    private static String matchPattern(Pattern p, String entity, String key) {
        JsonReader jreader = Json.createReader(new ByteArrayInputStream(entity.getBytes()));
        JsonArray jarray = jreader.readArray();
        JsonArrayBuilder jarrBuilder = Json.createArrayBuilder();
        for(int i = 0; i < jarray.size(); i++) {
            String tempVal = jarray.getJsonObject(i).getString(key);
            if(p.matcher(tempVal).matches()) {
                System.out.println("tempval: " + tempVal);
                jarrBuilder.add(jarray.getJsonObject(i));
            }
        }
        jarray = jarrBuilder.build();
        System.out.println(jarray.toString());
        return jarray.toString();
    }
}
