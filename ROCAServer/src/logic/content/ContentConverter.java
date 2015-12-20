package logic.content;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import logic.exception.ContactException;
import logic.validation.ContactValidation;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.*;

/**
 * Created by resul on 09/06/15.
 */
public class ContentConverter {

    private ObjectMapper objectMapper;

    public ContentConverter() {
        this.objectMapper = new ObjectMapper();
    }

    public String getJsonData(MultivaluedMap<String, String> data) throws JsonProcessingException {
        return getJsonData(generateBasicFormMap(data));
    }

    public String getJsonData(Map<String, String> data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
    /**
     * This converts String to Map
     * @param data Json as String
     * @return
     */
    public Map<String, String> getMapData(String data) throws IOException {
        return objectMapper.readValue(data,
                new TypeReference<Map<String, Object>>() {});
    }
    public Map<String, String> generateBasicFormMap(MultivaluedMap<String, String> formData) {
        Map<String, String> formMap = new HashMap<String, String>();
        Set<String> keys = formData.keySet();
        Iterator<String> keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            String s = keyIterator.next();
            formMap.put(s, formData.get(s).get(0));
        }
        return formMap;
    }

    /**
     * This method adds key/value to the json content
     * @param key the key
     * @param value the value
     * @param content the json content
     * @return the new content with added key value
     */
    public String addKeyValue(String key, String value, String content) throws IOException {
        Map<String, String> map = getMapData(content);
        map.put(key, value);
        return getJsonData(map);
    }

    public boolean containsPUT(Map<String, String> formMap) {
        if(formMap.containsKey("_method")) {
            if(formMap.get("_method").toUpperCase().equals("PUT")) {
                return true;
            }
        }
        return false;
    }

    public boolean containsDELETE(Map<String, String> formMap) {
        if(formMap.containsKey("_method")) {
            if(formMap.get("_method").toUpperCase().equals("DELETE")) {
                formMap.remove("_method");
                return true;
            }
        }
        return false;
    }

}
