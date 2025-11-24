package skillforge;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QuizService {
    private final JsonDatabaseManager db;

    public QuizService(JsonDatabaseManager db) {
        this.db = db;
    }

   

    public int gradeQuiz(Quiz quiz, List<Integer> studentAnswers) {
        if (quiz == null || quiz.getQuestions() == null) return 0;

        List<Integer> answers = (studentAnswers == null) ? new ArrayList<>() : studentAnswers;
        int total = quiz.getQuestions().size();
        if (total == 0) return 0;

        int correct = 0;
        for (int i = 0; i < total; i++) {
            int student = (i < answers.size()) ? answers.get(i) : -1;
            int correctIndex = quiz.getQuestions().get(i).getCorrectIndex();
            if (student == correctIndex) correct++;
        }
        return (int) Math.round((correct * 100.0) / total);
    }

    public boolean isPassed(Quiz quiz, int score) {
        return quiz != null && score >= quiz.getPassingScore();
    }


    public void saveQuizResult(String studentId, String courseId, String lessonId, int score, boolean passed) {
        JSONArray users = db.readUsers();

        JSONObject userObj = findUserById(users, studentId);
        if (userObj == null) {
            userObj = new JSONObject();
            userObj.put("id", studentId);
            userObj.put("progress", new JSONArray()); 
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

        JSONObject courseProgress = getOrCreateCourseProgress(userObj, Integer.parseInt(courseId));

        JSONArray scoresArray = courseProgress.optJSONArray("scores");
if (scoresArray == null) {
    scoresArray = new JSONArray();
    courseProgress.put("scores", scoresArray);
}

boolean found = false;
for (int i = 0; i < scoresArray.length(); i++) {
    JSONObject s = scoresArray.getJSONObject(i);
    if (s.optInt("lessonId", -1) == Integer.parseInt(lessonId)) {
        s.put("score", score);   // ✅ update score
        found = true;
        break;
    }
}
if (!found) {
    JSONObject newScore = new JSONObject();
    newScore.put("lessonId", Integer.parseInt(lessonId));
    newScore.put("score", score);
    scoresArray.put(newScore);
}

        
        JSONArray quizResults = courseProgress.optJSONArray("quizResults");
        if (quizResults == null) {
            quizResults = new JSONArray();
            courseProgress.put("quizResults", quizResults);
        }
        boolean qrFound = false;
        for (int i = 0; i < quizResults.length(); i++) {
            JSONObject qr = quizResults.getJSONObject(i);
            if (qr.optInt("lessonId", -1) == Integer.parseInt(lessonId)) {
                qr.put("attempts", attempts);
                qrFound = true;
                break;
            }
        }
        if (!qrFound) {
            JSONObject qr = new JSONObject();
            qr.put("lessonId", Integer.parseInt(lessonId));
            qr.put("attempts", attempts);
            quizResults.put(qr);
        }

       
        if (passed) {
    JSONArray completed = courseProgress.optJSONArray("completedLessonIds");
    if (completed == null) {
        completed = new JSONArray();
        courseProgress.put("completedLessonIds", completed);
    }
    int lessonIdInt = Integer.parseInt(lessonId);
    if (!completed.toList().contains(lessonIdInt)) {
        completed.put(lessonIdInt);
    }
}

   
        db.writeUsers(users);
    }

   
    public void incrementAttemptCount(String studentId, String courseId, String lessonId) {
        JSONArray users = db.readUsers();
        JSONObject userObj = findUserById(users, studentId);
        if (userObj == null) return;

        JSONObject courseProgress = getOrCreateCourseProgress(userObj, Integer.parseInt(courseId));

        JSONArray quizResults = courseProgress.optJSONArray("quizResults");
        if (quizResults == null) {
            quizResults = new JSONArray();
            courseProgress.put("quizResults", quizResults);
        }

        boolean found = false;
        for (int i = 0; i < quizResults.length(); i++) {
            JSONObject qr = quizResults.getJSONObject(i);
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
    }


    public boolean hasPassedLesson(String studentId, String courseId, String lessonId) {
        JSONArray users = db.readUsers();
        JSONObject user = findUserById(users, studentId);
        if (user == null) return false;

        JSONArray progress = user.optJSONArray("progress");
        if (progress == null) return false;

        int courseIdInt = Integer.parseInt(courseId);
        int lessonIdInt = Integer.parseInt(lessonId);

        for (Object o : progress) {
            JSONObject cp = (JSONObject) o;
            if (cp.optInt("courseId", -1) == courseIdInt) {
                JSONArray completed = cp.optJSONArray("completedLessonIds");
                return completed != null && completed.toList().contains(lessonIdInt);
            }
        }
        return false;
    }

    public int getAttemptCount(String studentId, String courseId, String lessonId) {
        JSONArray users = db.readUsers();
        JSONObject userObj = findUserById(users, studentId);
        if (userObj == null) return 0;

        JSONObject quizAttempts = userObj.optJSONObject("quizAttempts");
        if (quizAttempts == null) return 0;

        JSONObject courseObj = quizAttempts.optJSONObject(courseId);
        if (courseObj == null) return 0;

        JSONObject lessonObj = courseObj.optJSONObject(lessonId);
        if (lessonObj == null) return 0;

        return lessonObj.optInt("attempts", 0);
    }

    public int getStudentScore(String studentId, int courseId, int lessonId) {
        JSONArray users = db.readUsers();
        JSONObject user = findUserById(users, studentId);
        if (user == null) return 0;

        JSONArray progress = user.optJSONArray("progress");
        if (progress == null) return 0;

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
        return 0;
    }

 
    public void addQuestionToQuiz(Quiz quiz, String text, List<String> options, int correctIndex) {
        if (quiz == null) return;
        if (options == null) options = new ArrayList<>();
        Question q = new Question(text, options, correctIndex);
        quiz.getQuestions().add(q);
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

  
    private JSONObject findUserById(JSONArray users, String studentId) {
        for (Object o : users) {
            JSONObject user = (JSONObject) o;
            if (studentId.equals(user.optString("id"))) {
                return user;
            }
        }
        return null;
    }

    private JSONObject getOrCreateCourseProgress(JSONObject userObj, int courseIdInt) {
        JSONArray progress = userObj.optJSONArray("progress");
        if (progress == null) {
            progress = new JSONArray();
            userObj.put("progress", progress);
        }

        for (int i = 0; i < progress.length(); i++) {
            JSONObject cp = progress.getJSONObject(i);
            if (cp.optInt("courseId", -1) == courseIdInt) {
                return cp;
            }
        }

      
        JSONObject cp = new JSONObject();
        cp.put("courseId", courseIdInt);
        cp.put("completedLessonIds", new JSONArray());
        cp.put("scores", new JSONArray());
        cp.put("quizResults", new JSONArray());
        progress.put(cp);
        return cp;
    }
}