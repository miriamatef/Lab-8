package skillforge;

import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;


public class Lesson {
    private int lessonId;
    private String title;
    private String content;
    private List<String> resources;
    private Quiz quiz;

    public Lesson() {}
    public Lesson(int lessonId, String title, String content, List<String> resources) {
        this.lessonId = lessonId;
        this.title = title;
        this.content = content;
        this.resources = resources == null ? new ArrayList<>() : resources;
    }

    public int getLessonId() { return lessonId; }
    public void setLessonId(int lessonId) { this.lessonId = lessonId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getResources() { return resources; }
    public void setResources(List<String> resources) { this.resources = resources; }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
    

    @Override
    public String toString() { return title; }
 public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("lessonId", lessonId);
        obj.put("title", title);
        obj.put("content", content);

        JSONArray rArr = new JSONArray();
        for (String r : resources) rArr.put(r);
        obj.put("resources", rArr);

        if (quiz != null) {
            obj.put("quiz", quiz.toJson());
        }

        return obj;
    }

    public static Lesson fromJson(JSONObject obj) {
        Lesson l = new Lesson();

        l.lessonId = obj.optInt("lessonId", 0);
        l.title = obj.optString("title", "");
        l.content = obj.optString("content", "");

        l.resources = new ArrayList<>();
        JSONArray rArr = obj.optJSONArray("resources");
        if (rArr != null) {
            for (int i = 0; i < rArr.length(); i++) {
                l.resources.add(rArr.optString(i));
            }
        }

        if (obj.has("quiz") && !obj.isNull("quiz")) {
            l.quiz = Quiz.fromJson(obj.optJSONObject("quiz"));
        }

        return l;
    }
}
