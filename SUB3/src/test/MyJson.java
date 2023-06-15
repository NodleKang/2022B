package test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedList;

public class MyJson {

    public static JsonObject convertStringToJsonObject(String jsonStr) {
        Gson gson = new Gson();
        JsonObject jsonObj = gson.fromJson(jsonStr, JsonObject.class);
        return jsonObj;
    }

    public static String convertJsonObjectToString(JsonObject jsonObj) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(jsonObj);
        return jsonStr;
    }

    public static JsonArray convertStringToJsonArray(String jsonStr) {
        Gson gson = new Gson();
        JsonArray jsonArray = gson.fromJson(jsonStr, JsonArray.class);
        return jsonArray;
    }

    public static String convertJsonArrayToString(JsonArray jsonArray) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(jsonArray);
        return jsonStr;
    }

    public static LinkedList<String> convertJsonArrayToStringList(JsonArray jsonArray) {
        LinkedList<String> list = new LinkedList<String>();
        for (JsonElement element: jsonArray) {
            list.add(element.getAsString());
        }
        return list;
    }

}
