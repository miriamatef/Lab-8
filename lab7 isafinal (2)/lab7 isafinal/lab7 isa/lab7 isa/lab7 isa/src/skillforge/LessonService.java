package skillforge;

import org.json.*;
import java.util.*;

public class LessonService {

    private final JsonDatabaseManager db;

    public LessonService(JsonDatabaseManager db) {
        this.db = db;
    }

    public Lesson addLesson(int courseId, String title, String content, List<String> resources) {
        JSONArray courses = db.readCourses();
        for (Object o : courses) {
            JSONObject c = (JSONObject) o;
            if (c.optInt("id", -1) == courseId) {
                JSONArray lessons = c.optJSONArray("lessons");
                if (lessons == null) {
                    lessons = new JSONArray();
                    c.put("lessons", lessons);
                }
                for (Object l : lessons) {
                    if (title.equalsIgnoreCase(((JSONObject) l).optString("title", ""))) {
                        return null;
                    }
                }
                int lid = db.generateLessonId(lessons);
                JSONObject nl = new JSONObject();
                nl.put("id", lid);
                nl.put("title", title);
                nl.put("content", content == null ? "" : content);
                nl.put("resources", new JSONArray(resources == null ? new ArrayList<>() : resources));
                lessons.put(nl);
                db.writeCourses(courses);
                return new Lesson(lid, title, content, resources);
            }
        }
        return null;
    }

    public boolean editLesson(int courseId, int lessonId, String newTitle, String newContent, List<String> resources) {
        JSONArray courses = db.readCourses();
        for (Object o : courses) {
            JSONObject c = (JSONObject) o;
            if (c.optInt("id", -1) == courseId) {
                JSONArray lessons = c.optJSONArray("lessons");
                if (lessons == null) {
                    return false;
                }
                for (int i = 0; i < lessons.length(); i++) {
                    JSONObject l = lessons.getJSONObject(i);
                    if (l.optInt("id", -1) == lessonId) {
                        l.put("title", newTitle);
                        l.put("content", newContent);
                        l.put("resources", new JSONArray(resources == null ? new ArrayList<>() : resources));
                        db.writeCourses(courses);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean deleteLesson(int courseId, int lessonId) {
        JSONArray courses = db.readCourses();
        for (Object o : courses) {
            JSONObject c = (JSONObject) o;
            if (c.optInt("id", -1) == courseId) {
                JSONArray lessons = c.optJSONArray("lessons");
                if (lessons == null) {
                    return false;
                }
                for (int i = 0; i < lessons.length(); i++) {
                    JSONObject l = lessons.getJSONObject(i);
                    if (l.optInt("id", -1) == lessonId) {
                        lessons.remove(i);
                        db.writeCourses(courses);
                        JSONArray users = db.readUsers();
                        for (int j = 0; j < users.length(); j++) {
                            JSONObject u = users.getJSONObject(j);
                            JSONArray progress = u.optJSONArray("progress");
                            if (progress == null) {
                                continue;
                            }
                            for (int k = 0; k < progress.length(); k++) {
                                JSONObject p = progress.getJSONObject(k);
                                if (p.optInt("courseId", -1) == courseId) {
                                    JSONArray comp = p.optJSONArray("completedLessonIds");
                                    if (comp == null) {
                                        continue;
                                    }
                                    JSONArray na = new JSONArray();
                                    for (Object id : comp) {
                                        if (!Integer.valueOf(lessonId).equals(id)) {
                                            na.put(id);
                                        }
                                    }
                                    p.put("completedLessonIds", na);
                                }
                            }
                        }
                        db.writeUsers(users);
                        return true;
                    }
                }
            }
        }
        return false;
    }
public boolean markLessonCompleted(int courseId, int lessonId, String studentId, int score) {
    CourseService courseService = new CourseService(db); 
    Quiz quiz = courseService.getQuiz(courseId, lessonId);

    if (quiz != null && score >= quiz.getPassingScore()) {
        JSONArray users = db.readUsers();
        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (studentId.equals(u.optString("id", ""))) {
                JSONArray progress = u.optJSONArray("progress");
                if (progress != null) {
                    for (Object o : progress) {
                        JSONObject p = (JSONObject) o;
                        if (p.optInt("courseId") == courseId) {
                            JSONArray completed = p.optJSONArray("completedLessonIds");
                            completed.put(lessonId);
                            db.writeUsers(users);
                            return true;
                        }
                    }
                }
            }
        }
    }
    return false; // quiz not passed, lesson not completed
}
   
public List<Lesson> getLessons(int courseId) {
    List<Lesson> result = new ArrayList<>();
    JSONArray courses = db.readCourses();
    for (Object o : courses) {
        JSONObject c = (JSONObject) o;
        if (c.optInt("id", -1) == courseId) {
            JSONArray lessons = c.optJSONArray("lessons");
            if (lessons != null) {
                for (Object l : lessons) {
                    JSONObject lessonJson = (JSONObject) l;
                    int id = lessonJson.optInt("id", -1);
                    String title = lessonJson.optString("title", "");
                    String content = lessonJson.optString("content", "");
                    List<String> resources = new ArrayList<>();
                    JSONArray resArray = lessonJson.optJSONArray("resources");
                    if (resArray != null) {
                        for (Object r : resArray) resources.add((String) r);
                    }

                    Lesson lesson = new Lesson(id, title, content, resources);

                   
                    if (lessonJson.has("quiz")) {
                        JSONObject quizJson = lessonJson.getJSONObject("quiz");
                        Quiz quiz = Quiz.fromJson(quizJson);
                        lesson.setQuiz(quiz);
                    }

                    result.add(lesson);
                }
            }
        }
    }
    return result;
}



}