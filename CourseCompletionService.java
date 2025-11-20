/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;

/**
 *
 * @author karen
 */


import org.json.JSONArray;
import org.json.JSONObject;

public class CourseCompletionService {

    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();

    public CourseCompletionService() {}

    
    public boolean isCourseCompleted(String studentId, String courseId) {

        JSONArray users = db.readUsers();
        JSONArray courses = db.readCourses();

        JSONObject student = findById(users, studentId);
        JSONObject course = findById(courses, courseId);

        if (student == null || course == null) return false;

        JSONArray lessons = course.optJSONArray("lessons");
        if (lessons == null) return false;

        for (Object o : lessons) {
            JSONObject lesson = (JSONObject) o;

            int lessonId = lesson.optInt("id");

           
            JSONObject quiz = lesson.optJSONObject("quiz");
            if (quiz != null) {

                String quizId = quiz.optString("id");
                int passingScore = quiz.optInt("passingScore");

                double bestScore = getBestQuizScore(student, quizId);

                if (bestScore < passingScore) {
                    return false; 
                }

            } else {
               
                if (!isLessonCompleted(student, courseId, lessonId)) {
                    return false;
                }
            }
        }

        return true;
    }

   
    private JSONObject findById(JSONArray array, String id) {
        for (Object o : array) {
            JSONObject obj = (JSONObject) o;
            if (obj.optString("id").equals(id)) return obj;
        }
        return null;
    }

    
    private double getBestQuizScore(JSONObject student, String quizId) {

        JSONArray attempts = student.optJSONArray("quizAttempts");
        if (attempts == null) return -1; 

        double best = -1;

        for (Object o : attempts) {
            JSONObject attempt = (JSONObject) o;
            if (quizId.equals(attempt.optString("quizId"))) {
                best = Math.max(best, attempt.optDouble("score"));
            }
        }

        return best;
    }

   
    private boolean isLessonCompleted(JSONObject student, String courseId, int lessonId) {

        JSONArray completedLessons = student.optJSONArray("completedLessons");
        if (completedLessons == null) return false;

        for (Object o : completedLessons) {
            JSONObject entry = (JSONObject) o;

            if (courseId.equals(entry.optString("courseId"))
                    && lessonId == entry.optInt("lessonId")) {
                return true;
            }
        }

        return false;
    }
}
