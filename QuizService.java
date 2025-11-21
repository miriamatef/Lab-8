/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7.isa;

import lab7.isa.Quiz;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mariam Elshamy
 */
public class QuizService {
 private final JsonDatabaseManager db;

    public QuizService(JsonDatabaseManager db) {
        this.db = db;
    }
public int gradeQuiz(Quiz quiz, List<Integer> studentAnswers) {
        if (quiz == null || quiz.getQuestions() == null) return 0;
        List<Integer> answers = studentAnswers == null ? new ArrayList<>() : studentAnswers;
        int total = quiz.getQuestions().size();
        int correct = 0;
        for (int i = 0; i < total; i++) {
            int student = (i < answers.size()) ? answers.get(i) : -1;
            int correctIndex = quiz.getQuestions().get(i).getCorrectIndex();
            if (student == correctIndex) correct++;
        }
        if (total == 0) return 0;
        return (int) Math.round((correct * 100.0) / total);
    }
public boolean isPassed(Quiz quiz, int score) {
    if (quiz == null) return false;
    return score >= quiz.getPassingScore();
}

public void incrementAttemptCount(String studentId, String courseId, String lessonId) {
    JSONArray users = db.readUsers();
    for (int i = 0; i < users.length(); i++) {
        JSONObject u = users.getJSONObject(i);
        if (studentId.equals(u.optString("id", ""))) {
            JSONArray progress = u.optJSONArray("progress");
            if (progress != null) {
                for (Object o : progress) {
                    JSONObject cp = (JSONObject) o;
                    if (cp.optInt("courseId", -1) == Integer.parseInt(courseId)) {
                        // find or create quizResults array
                        JSONArray quizResults = cp.optJSONArray("quizResults");
                        if (quizResults == null) {
                            quizResults = new JSONArray();
                            cp.put("quizResults", quizResults);
                        }

                        boolean found = false;
                        for (int j = 0; j < quizResults.length(); j++) {
                            JSONObject qr = quizResults.getJSONObject(j);
                            if (qr.optInt("lessonId", -1) == Integer.parseInt(lessonId)) {
                                int attempts = qr.optInt("attempts", 0);
                                qr.put("attempts", attempts + 1);
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            JSONObject qr = new JSONObject();
                            qr.put("lessonId", Integer.parseInt(lessonId));
                            qr.put("attempts", 1);
                            quizResults.put(qr);
                        }

                        db.writeUsers(users);
                        return;
                    }
                }
            }
        }
    }
}


    public void saveQuizResult(String studentId, String courseId, String lessonId, int score, boolean passed) {
        JSONArray users = db.readUsers();
        JSONObject userObj = findUserById(users, studentId);
        if (userObj == null) {
            userObj = new JSONObject();
            userObj.put("id", studentId);
            users.put(userObj);
        }

        JSONObject quizAttempts = userObj.optJSONObject("quizAttempts");
        if (quizAttempts == null) {
            quizAttempts = new JSONObject();
            userObj.put("quizAttempts", quizAttempts);
        }

        JSONObject courseObj = quizAttempts.optJSONObject(courseId);
        if (courseObj == null) {
            courseObj = new JSONObject();
            quizAttempts.put(courseId, courseObj);
        }

        JSONObject lessonObj = courseObj.optJSONObject(lessonId);
        int attempts = (lessonObj != null) ? lessonObj.optInt("attempts", 0) : 0;
        if (lessonObj == null) {
            lessonObj = new JSONObject();
        }

        attempts += 1;
        lessonObj.put("score", score);
        lessonObj.put("attempts", attempts);
        lessonObj.put("passed", passed);
        lessonObj.put("lastAttemptDate", LocalDate.now().toString());

        courseObj.put(lessonId, lessonObj);
        quizAttempts.put(courseId, courseObj);
        userObj.put("quizAttempts", quizAttempts);

        db.writeUsers(users);
    }
    public boolean hasPassedLesson(String studentId, String courseId, String lessonId) {
    JSONArray users = db.readUsers();
    for (int i = 0; i < users.length(); i++) {
        JSONObject u = users.getJSONObject(i);
        if (studentId.equals(u.optString("id", ""))) {
            JSONArray progress = u.optJSONArray("progress");
            if (progress != null) {
                for (Object o : progress) {
                    JSONObject cp = (JSONObject) o;
                    if (courseId.equals(String.valueOf(cp.optInt("courseId", -1)))) {
                        JSONArray completed = cp.optJSONArray("completedLessonIds");
                        if (completed != null && completed.toList().contains(Integer.parseInt(lessonId))) {
                            return true;
                        }
                    }
                }
            }
        }
    }
    return false;
}


    public int getAttemptCount(String studentId, String courseId, String lessonId) {
        JSONObject userObj = findUserById(db.readUsers(), studentId);
        if (userObj == null) return 0;
        JSONObject quizAttempts = userObj.optJSONObject("quizAttempts");
        if (quizAttempts == null) return 0;
        JSONObject courseObj = quizAttempts.optJSONObject(courseId);
        if (courseObj == null) return 0;
        JSONObject lessonObj = courseObj.optJSONObject(lessonId);
        if (lessonObj == null) return 0;
        return lessonObj.optInt("attempts", 0);
    }
    private JSONObject findUserById(JSONArray users, String studentId) {
        for (Object o : users) {
            JSONObject user = (JSONObject) o;
            if (studentId.equals(user.optString("id"))) {
                return user;
            }
        }
        return null;
    }
    public void addQuestionToQuiz(Quiz quiz, String text, List<String> options, int correctIndex) {
    Question q = new Question(text, options, correctIndex);
    quiz.getQuestions().add(q);
}
public int getStudentScore(String studentId, int courseId, int lessonId) {
    JSONArray users = db.readUsers();
    for (int i = 0; i < users.length(); i++) {
        JSONObject u = users.getJSONObject(i);
        if (studentId.equals(u.optString("id", ""))) {
            JSONArray progress = u.optJSONArray("progress");
            if (progress != null) {
                for (Object o : progress) {
                    JSONObject cp = (JSONObject) o;
                    if (cp.optInt("courseId", -1) == courseId) {
                        JSONArray scores = cp.optJSONArray("scores");
                        if (scores != null) {
                            for (Object s : scores) {
                                JSONObject scoreObj = (JSONObject) s;
                                if (scoreObj.optInt("lessonId", -1) == lessonId) {
                                    return scoreObj.optInt("score", 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return 0;
}
public void saveQuiz(String courseId, String lessonId, Quiz quiz) {
    JSONArray courses = db.readCourses();
    for (int i = 0; i < courses.length(); i++) {
        JSONObject courseJson = courses.getJSONObject(i);
        if (courseId.equals(courseJson.optString("id"))) {
            JSONArray lessons = courseJson.optJSONArray("lessons");
            if (lessons != null) {
                for (int j = 0; j < lessons.length(); j++) {
                    JSONObject lessonJson = lessons.getJSONObject(j);
                    if (lessonId.equals(lessonJson.optString("id"))) {
                        lessonJson.put("quiz", quiz.toJson());
                        db.writeCourses(courses);
                        return;
                    }
                }
            }
        }
    }
}


}
