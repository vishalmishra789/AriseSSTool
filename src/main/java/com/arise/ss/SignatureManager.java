package com.arise.ss;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SignatureManager {
    private List<String> redList = new ArrayList<>();
    private List<String> yellowList = new ArrayList<>();

    public SignatureManager() {
        try {
            InputStream is = getClass().getResourceAsStream("/signatures.json");
            String jsonTxt = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(jsonTxt);

            JSONArray red = obj.getJSONArray("red");
            for (int i = 0; i < red.length(); i++) redList.add(red.getString(i).toLowerCase());

            JSONArray yellow = obj.getJSONArray("yellow");
            for (int i = 0; i < yellow.length(); i++) yellowList.add(yellow.getString(i).toLowerCase());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getRedList() { return redList; }
    public List<String> getYellowList() { return yellowList; }
}
