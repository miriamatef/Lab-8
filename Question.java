/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7.isa;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Mariam Elshamy
 */
public class Question {
    private String text;
    private List<String> options;
    private int index;
    public Question() {
        this.options = new ArrayList<>();
    }

    public Question(String text, List<String> options, int correctIndex) {
        this.text = text;
        this.options = options;
        this.index = correctIndex;
    }
     public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectIndex() { return index; }
    public void setCorrectIndex(int correctIndex) { this.index = correctIndex; }
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("text", text);
        obj.put("correctIndex", index);
        JSONArray arr = new JSONArray();
        for (String opt : options) arr.put(opt);
        obj.put("options", arr);
        return obj;
    }
    public static Question fromJson(JSONObject obj) {
        Question q = new Question();
        q.text = obj.optString("text", "");
        q.index = obj.optInt("correctIndex", 0);
        q.options = new ArrayList<>();
        JSONArray arr = obj.optJSONArray("options");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) q.options.add(arr.optString(i));
        }
        return q;
    }
}
