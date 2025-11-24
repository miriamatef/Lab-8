package skillforge;

import org.json.*;
import java.util.ArrayList;
import java.util.List;

public class Student extends User {

    public Student(JSONObject studentData) {
        super(studentData);

        if (!studentData.has("role")) studentData.put("role", "student");
        if (!studentData.has("enrolledCourses")) studentData.put("enrolledCourses", new JSONArray());
        if (!studentData.has("progress")) studentData.put("progress", new JSONArray());
    }

    public Student(String userId, String username, String email, String passwordHash) {
        super(new JSONObject());
        JSONObject data = toJSON();

        data.put("id", userId);
        data.put("role", "student");
        data.put("username", username);
        data.put("email", email);
        data.put("password", passwordHash);
        data.put("enrolledCourses", new JSONArray());
        data.put("progress", new JSONArray());
    }

    public Student() {
        super(new JSONObject());
        JSONObject data = toJSON();

        data.put("role", "student");
        data.put("enrolledCourses", new JSONArray());
        data.put("progress", new JSONArray());
    }


    public String getPasswordHash() {
        return toJSON().optString("password", "");
    }

    public List<Integer> getEnrolledCourses() {
        List<Integer> courses = new ArrayList<>();
        JSONArray arr = toJSON().optJSONArray("enrolledCourses");

        if (arr != null) {
            for (Object obj : arr) {
                try { courses.add(Integer.parseInt(obj.toString())); }
                catch (Exception ignored) {}
            }
        }
        return courses;
    }

    public List<Progress> getProgress() {
        List<Progress> progressList = new ArrayList<>();
        JSONArray arr = toJSON().optJSONArray("progress");

        if (arr != null) {
            for (Object obj : arr) {
                if (obj instanceof JSONObject jsonObj) {
                    progressList.add(new Progress(jsonObj));
                }
            }
        }
        return progressList;
    }



    public void setPasswordHash(String hash) {
        toJSON().put("password", hash);
    }

    public void setEnrolledCourses(List<Integer> enrolledCourses) {
        JSONArray arr = new JSONArray();
        for (Integer id : enrolledCourses) arr.put(id);
        toJSON().put("enrolledCourses", arr);
    }

    public void setProgress(List<Progress> progress) {
        JSONArray arr = new JSONArray();
        for (Progress p : progress) arr.put(p.toJSON());
        toJSON().put("progress", arr);
    }


    public void enrollCourse(int courseId) {
        JSONObject data = toJSON();

        JSONArray courses = data.optJSONArray("enrolledCourses");
        if (courses == null) {
            courses = new JSONArray();
            data.put("enrolledCourses", courses);
        }


        for (Object o : courses) {
            if (Integer.parseInt(o.toString()) == courseId) return;
        }

        courses.put(courseId);


        JSONArray progress = data.optJSONArray("progress");
        if (progress == null) {
            progress = new JSONArray();
            data.put("progress", progress);
        }

        for (Object o : progress) {
            if (o instanceof JSONObject prog &&
                prog.optInt("courseId") == courseId) return;
        }

        progress.put(new Progress(courseId).toJSON());
    }

    public Progress getProgressForCourse(int courseId) {
        JSONArray arr = toJSON().optJSONArray("progress");
        if (arr == null) return null;

        for (Object o : arr) {
            if (o instanceof JSONObject obj &&
                obj.optInt("courseId") == courseId) {
                return new Progress(obj);
            }
        }
        return null;
    }

    public void unenrollCourse(int courseId) {
        JSONObject data = toJSON();


        JSONArray newCourses = new JSONArray();
        for (Object o : data.optJSONArray("enrolledCourses")) {
            if (Integer.parseInt(o.toString()) != courseId)
                newCourses.put(o);
        }
        data.put("enrolledCourses", newCourses);

        JSONArray newProg = new JSONArray();
        for (Object o : data.optJSONArray("progress")) {
            JSONObject prog = (JSONObject) o;
            if (prog.optInt("courseId") != courseId)
                newProg.put(prog);
        }
        data.put("progress", newProg);
    }

    public boolean isEnrolledIn(int courseId) {
        return getEnrolledCourses().contains(courseId);
    }

    @Override
    public String toString() {
        return getUsername() + " (Student)";
    }
}