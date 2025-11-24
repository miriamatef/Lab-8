/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Mariam Elshamy
 */
public class Quiz {
    // Metadata
    private String quizId;
    private String lessonId;
    private String title;
    private int passingScore; 

   
    private List<Question> questions;

 
    public Quiz() {
        this.questions = new ArrayList<>();
        this.passingScore = 50; 
        this.title = "";
    }

    public Quiz(String quizId, String lessonId, String title, int passingScore, List<Question> questions) {
        this.quizId = quizId;
        this.lessonId = lessonId;
        this.title = title == null ? "" : title;
        this.setPassingScore(passingScore);
        this.questions = (questions == null) ? new ArrayList<>() : questions;
    }

    
    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = (title == null) ? "" : title; }

    public int getPassingScore() { return passingScore; }
    public void setPassingScore(int passingScore) {
        
        if (passingScore < 0) passingScore = 0;
        if (passingScore > 100) passingScore = 100;
        this.passingScore = passingScore;
    }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) {
        this.questions = (questions == null) ? new ArrayList<>() : questions;
    }

   
    public int getQuestionCount() { return questions.size(); }
    public boolean isEmpty() { return questions == null || questions.isEmpty(); }

  
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("quizId", quizId);
        obj.put("lessonId", lessonId);
        obj.put("title", title);
        obj.put("passingScore", passingScore);

        JSONArray arr = new JSONArray();
        if (questions != null) {
            for (Question q : questions) {
                arr.put(q.toJson());
            }
        }
        obj.put("questions", arr);
        return obj;
    }
    public void addQuestion(String text, List<String> options, int correctIndex) {
    Question q = new Question(text, options, correctIndex);
    this.questions.add(q);
}


    public static Quiz fromJson(JSONObject json) {
    Quiz quiz = new Quiz();
    quiz.setLessonId(json.optString("lessonId", ""));

    JSONArray questionsArray = json.optJSONArray("questions");
    if (questionsArray != null) {
        for (Object o : questionsArray) {
            JSONObject q = (JSONObject) o;
            String text = q.optString("text", "");
            List<String> options = new ArrayList<>();
            JSONArray opts = q.optJSONArray("options");
            for (Object opt : opts) options.add((String) opt);
            int correct = q.optInt("correctIndex", 0);
            quiz.addQuestion(text, options, correct);


        }
    }

    quiz.setPassingScore(json.optInt("passingScore", 0));
    return quiz;
}
    
    private boolean taken;
private boolean passed;
private int lastScore;
public void recordAttempt(int score) {
    this.taken = true;
    this.lastScore = score;
    this.passed = score >= passingScore;
}
public boolean isTaken() { return taken; }
public boolean isPassed() { return passed; }
public int getLastScore() { return lastScore; }


}