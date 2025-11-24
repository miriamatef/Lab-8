package skillforge;

import org.json.*;
import java.util.ArrayList;
import java.util.List;

public class Instructor extends User {

    public Instructor(JSONObject instructorData) {
        super(instructorData);

        if (!instructorData.has("role")) {
            instructorData.put("role", "instructor");
        }

        if (!instructorData.has("createdCourses")) {
            instructorData.put("createdCourses", new JSONArray());
        }
    }

    public Instructor(String userId, String username, String email, String passwordHash) {
        super(new JSONObject());

        JSONObject data = toJSON();
        data.put("id", userId);
        data.put("role", "instructor");
        data.put("username", username);
        data.put("email", email);
        data.put("password", passwordHash);
        data.put("createdCourses", new JSONArray());
    }


    public String getPasswordHash() {
        return toJSON().optString("password", "");
    }

    public List<Integer> getCreatedCourses() {
        List<Integer> courses = new ArrayList<>();
        JSONArray array = toJSON().optJSONArray("createdCourses");

        if (array != null) {
            for (Object obj : array) {
                try {
                    courses.add(Integer.parseInt(obj.toString()));
                } catch (Exception ignored) {}
            }
        }
        return courses;
    }

    public void setPasswordHash(String hash) {
        toJSON().put("password", hash);
    }

    public void setCreatedCourses(List<Integer> createdCourses) {
        JSONArray arr = new JSONArray();
        for (Integer id : createdCourses) arr.put(id);
        toJSON().put("createdCourses", arr);
    }


    public void addCourse(int courseId) {
        JSONArray arr = toJSON().optJSONArray("createdCourses");
        if (arr == null) {
            arr = new JSONArray();
            toJSON().put("createdCourses", arr);
        }

        for (Object o : arr) {
            if (Integer.parseInt(o.toString()) == courseId) return;
        }

        arr.put(courseId);
    }

    public void removeCourse(int courseId) {
        JSONArray oldArr = toJSON().optJSONArray("createdCourses");
        if (oldArr == null) return;

        JSONArray newArr = new JSONArray();
        for (Object o : oldArr) {
            if (Integer.parseInt(o.toString()) != courseId) {
                newArr.put(o);
            }
        }

        toJSON().put("createdCourses", newArr);
    }

    @Override
    public String toString() {
        return getUsername() + " (Instructor)";
    }
}