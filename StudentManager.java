package skillforge;

import org.json.*;
import java.util.*;

public class StudentManager {
    private final JsonDatabaseManager db;

    public StudentManager(JsonDatabaseManager db) { 
        this.db = db; 
    }

    public List<Course> browseCourses() {
        JSONArray arr = db.readCourses();
        List<Course> res = new ArrayList<>();
        for (Object o : arr) res.add(jsonToCourse((JSONObject) o));
        return res;
    }

    public List<Course> getEnrolledCourses(String studentId) {
        JSONArray users = db.readUsers();
        JSONObject student = null;
        for (Object o : users) {
            JSONObject u = (JSONObject) o;
            if (studentId.equals(u.optString("id", "")) && 
                "student".equalsIgnoreCase(u.optString("role", ""))) {
                student = u; 
                break;
            }
        }
        if (student == null) return new ArrayList<>();
        JSONArray enrolled = student.optJSONArray("enrolledCourses");
        if (enrolled == null) return new ArrayList<>();

        List<Course> res = new ArrayList<>();
        JSONArray courses = db.readCourses();
        for (Object cobj : courses) {
            JSONObject c = (JSONObject) cobj;
            int cid = c.optInt("id", -1);
            for (Object id : enrolled) {
                if (Integer.valueOf(cid).equals(id)) res.add(jsonToCourse(c));
            }
        }
        return res;
    }

    public List<Lesson> getLessonsForCourse(int courseId) {
        JSONArray courses = db.readCourses();
        for (Object o : courses) {
            JSONObject c = (JSONObject) o;
            if (c.optInt("id", -1) == courseId) {
                JSONArray la = c.optJSONArray("lessons");
                List<Lesson> res = new ArrayList<>();
                if (la != null) {
                    for (Object l : la) res.add(jsonToLesson((JSONObject) l));
                }
                return res;
            }
        }
        return new ArrayList<>();
    }

    public boolean enroll(String studentId, int courseId) {
        CourseService cs = new CourseService(db);
        return cs.enrollStudent(courseId, studentId);
    }

    public boolean markLessonCompleted(String studentId, int courseId, int lessonId) {
    JSONArray users = db.readUsers();
    boolean updated = false;

    for (int i = 0; i < users.length(); i++) {
        JSONObject user = users.getJSONObject(i);
        if (studentId.equals(user.optString("id", "")) &&
            "student".equalsIgnoreCase(user.optString("role", ""))) {

            JSONArray completedArr = user.optJSONArray("completedLessons");
            if (completedArr == null) {
                completedArr = new JSONArray();
                user.put("completedLessons", completedArr);
            }

            JSONObject courseEntry = null;
            for (Object obj : completedArr) {
                JSONObject entry = (JSONObject) obj;
                if (entry.optInt("courseId", -1) == courseId) {
                    courseEntry = entry;
                    break;
                }
            }

            if (courseEntry == null) {
                courseEntry = new JSONObject();
                courseEntry.put("courseId", courseId);
                courseEntry.put("lessons", new JSONArray());
                completedArr.put(courseEntry);
            }

            JSONArray lessons = courseEntry.optJSONArray("lessons");
            if (lessons == null) {
                lessons = new JSONArray();
                courseEntry.put("lessons", lessons);
            }

            boolean alreadyDone = false;
            for (Object lid : lessons) {
                if (Integer.valueOf(lessonId).equals(lid)) {
                    alreadyDone = true;
                    break;
                }
            }

            if (!alreadyDone) {
                lessons.put(lessonId);
                updated = true;
            }

            break;
        }
    }

    if (updated) {
        db.writeUsers(users); // Save changes to database
    }

    return updated;
}


    public boolean registerStudent(String username, String email, String password) {
        UserService us = new UserService(db);
        return us.signup("student", username, email, password);
    }

    // helpers
    private Course jsonToCourse(JSONObject json) {
        int id = json.optInt("id", -1);
        String title = json.optString("title", "");
        String desc = json.optString("description", "");
        String instr = json.optString("instructorId", "");
        String status = json.optString("status");
        List<Lesson> lessons = new ArrayList<>();
        JSONArray la = json.optJSONArray("lessons");
        if (la != null) {
            for (Object o : la) lessons.add(jsonToLesson((JSONObject) o));
        }

        List<String> students = new ArrayList<>();
        JSONArray sa = json.optJSONArray("students");
        if (sa != null) {
            for (Object s : sa) students.add(String.valueOf(s));
        }

        return new Course(id, title, desc, instr, lessons, students, status);
       
    }

    private Lesson jsonToLesson(JSONObject json) {
        int id = json.optInt("id", -1);
        String title = json.optString("title", "");
        String content = json.optString("content", "");
        
        List<String> resources = new ArrayList<>();
        JSONArray ra = json.optJSONArray("resources");
        if (ra != null) {
            for (Object r : ra) resources.add(String.valueOf(r));
        }

        return new Lesson(id, title, content, resources);
    }

   public List<Integer> getCompletedLessons(String studentId, int courseId) {
    JSONArray users = db.readUsers();
    JSONObject student = null;
    for (Object o : users) {
        JSONObject u = (JSONObject) o;
        if (studentId.equals(u.optString("id", "")) && 
            "student".equalsIgnoreCase(u.optString("role", ""))) {
            student = u; 
            break;
        }
    }
    if (student == null) return new ArrayList<>();

 
    JSONArray completedArr = student.optJSONArray("progress");
    if (completedArr == null) return new ArrayList<>();

    List<Integer> res = new ArrayList<>();
    for (Object obj : completedArr) {
        JSONObject comp = (JSONObject) obj;
        if (comp.optInt("courseId", -1) == courseId) {
            JSONArray lessons = comp.optJSONArray("completedLessonIds");
            if (lessons != null) {
                for (Object lid : lessons) {
                    res.add(Integer.valueOf(String.valueOf(lid)));
                }
            }
        }
    }
    return res;
}

}