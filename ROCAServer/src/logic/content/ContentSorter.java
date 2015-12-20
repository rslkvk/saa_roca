package logic.content;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by resul on 15/06/15.
 */
public class ContentSorter {

    public static String sort(JsonArray entity, String criteria) throws IOException {
        JsonReader reader = Json.createReader(
                new ByteArrayInputStream(criteria.getBytes()));
        JsonObject jobj = reader.readObject();
        String sortby = jobj.getString("sortby");
        String value = jobj.getString("value");

        JsonArrayBuilder jarrBuilder = Json.createArrayBuilder();

        //new map for the full sorted result.
        Map<String, String> sortedFullMap = new HashMap<String, String>();

        //Only the ID as key and VALUE as value, which is to be sorted (i.e. <_id, first_name>)
        //After a sorting, the toSort variable contains sorted values.
        Map<String, String> sortValueMap = toSort(sortby, value, entity);

        //JsonArray entity to JsonObject entity. Only JsonObject can be converted to Map.
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        for (int i = 0; i < entity.size(); i++) {
            jsonObjectBuilder.add(
                    entity.getJsonObject(i).getString("_link"), entity.getJsonObject(i));
        }
        JsonObject jsonObject = jsonObjectBuilder.build();
        System.out.println(jsonObject.toString());
        //ContentConverter has the methods to convert any json type
        ContentConverter contentConverter = new ContentConverter();
        //make from unsorted json object an unsorted map.
        Map<String, String> unsortedMap = contentConverter.getMapData(jsonObject.toString());
        Iterator<String> keys = sortValueMap.keySet().iterator();
        //get the key step by step from sorted map. Take the value from unsorted map with the sorted key.
        JsonValue val;
        while (keys.hasNext()) {
            String key = keys.next();
            Object o = unsortedMap.get(key);
            LinkedHashMap<String, String> s = ((LinkedHashMap<String, String>) o);
            Iterator<String> lkey = s.keySet().iterator();
            jsonObjectBuilder = Json.createObjectBuilder();
            while (lkey.hasNext()) {
                String llkey = lkey.next();
                jsonObjectBuilder.add(llkey, s.get(llkey).toString());
            }
            jarrBuilder.add(jsonObjectBuilder.build());
        }
        return jarrBuilder.build().toString();
    }

    private static Map<String, String> toSort(String sortby, String value, JsonArray entity) {
        Map<String, String> toSort = new HashMap<String, String>();

        //make new map, which have to sort
        for (int i = 0; i < entity.size(); i++) {
            String key = entity.getJsonObject(i).getString("_link");
            String val = entity.getJsonObject(i).getString(sortby);
            toSort.put(key, val);
        }

        // check the sequence (asc/desc)
        if (value.equals("desc")) {
            toSort = sortByComparator(toSort, true);
        } else {
            toSort = sortByComparator(toSort, false);
        }

        return toSort;
    }

    private static Map<String, String> sortByComparator(Map<String, String> unsortedMap, boolean desc) {
        // Convert map to list.
        List<Map.Entry<String, String>> sortList = new LinkedList<
                Map.Entry<String, String>>(unsortedMap.entrySet());

        // make new comparator and sort list
        Collections.sort(sortList, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        if (desc) {
            Collections.reverse(sortList);
        }

        // Convert list to map
        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        Iterator<Map.Entry<String, String>> iter = sortList.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
