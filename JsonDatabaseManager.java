package skillforge;


import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonDatabaseManager {

    private static JsonDatabaseManager instance;
    private static final Logger logger = Logger.getLogger(JsonDatabaseManager.class.getName());

    private final File usersFile = new File("users.json");
    private final File coursesFile = new File("courses.json");

    private JsonDatabaseManager() {
        ensureFile(usersFile);
        ensureFile(coursesFile);
    }

    public static synchronized JsonDatabaseManager getInstance() {
        if (instance == null) instance = new JsonDatabaseManager();
        return instance;
    }

    private void ensureFile(File f) {
        try {
            if (!f.exists()) {
                try (FileWriter w = new FileWriter(f)) {
                    w.write("[]");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create file: " + f.getName(), e);
        }
    }

    private JSONArray readArrayFromFile(File f) {
        if (!f.exists() || f.length() == 0) return new JSONArray();
        try (FileReader reader = new FileReader(f)) {
            return new JSONArray(new JSONTokener(reader));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading " + f.getName(), e);
            return new JSONArray();
        }
    }

    private void writeArrayToFile(File f, JSONArray arr) {
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(arr.toString(4)); // pretty-print
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing " + f.getName(), e);
        }
    }

    /** USERS **/

    public synchronized JSONArray readUsers() {
        return readArrayFromFile(usersFile);
    }

    public synchronized void writeUsers(JSONArray users) {
        writeArrayToFile(usersFile, users);
    }

    public synchronized void saveUsers() {
        JSONArray users = readUsers();
        writeUsers(users);
    }

    public synchronized JSONObject findUserById(String userId) {
        for (Object o : readUsers()) {
            JSONObject u = (JSONObject) o;
            if (userId.equals(u.optString("id"))) return u;
        }
        return null;
    }

    public synchronized void addUser(JSONObject user) {
        JSONArray users = readUsers();
        users.put(user);
        writeUsers(users);
    }

    public synchronized void updateUser(JSONObject user) {
        JSONArray users = readUsers();
        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (u.getString("id").equals(user.getString("id"))) {
                users.put(i, user);
                writeUsers(users);
                return;
            }
        }
        users.put(user); // add if not found
        writeUsers(users);
    }

    public synchronized void addCertificateToUser(String studentId, JSONObject certJson) {
        JSONArray users = readUsers();
        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (studentId.equals(u.optString("id"))) {
                JSONArray certs = u.optJSONArray("certificates");
                if (certs == null) certs = new JSONArray();
                certs.put(certJson);
                u.put("certificates", certs);
                users.put(i, u);
                writeUsers(users);
                return;
            }
        }
    }

    public synchronized JSONArray getCertificatesForUser(String studentId) {
        JSONObject user = findUserById(studentId);
        if (user == null) return new JSONArray();
        return user.optJSONArray("certificates") != null ? user.getJSONArray("certificates") : new JSONArray();
    }

    /** COURSES **/

    public synchronized JSONArray readCourses() {
        return readArrayFromFile(coursesFile);
    }

    public synchronized void writeCourses(JSONArray courses) {
        writeArrayToFile(coursesFile, courses);
    }

    public synchronized JSONObject findCourseById(String courseId) {
        for (Object o : readCourses()) {
            JSONObject c = (JSONObject) o;
            if (courseId.equals(c.optString("id"))) return c;
        }
        return null;
    }

    public synchronized void addCourse(JSONObject course) {
        JSONArray courses = readCourses();
        courses.put(course);
        writeCourses(courses);
    }

    /** IDs **/

    public synchronized String generateUserId() {
        JSONArray users = readUsers();
        String id;
        boolean ok;
        do {
            id = "U" + (1000 + (int) (Math.random() * 9000));
            ok = true;
            for (Object o : users) {
                JSONObject u = (JSONObject) o;
                if (id.equals(u.optString("id", ""))) {
                    ok = false;
                    break;
                }
            }
        } while (!ok);
        return id;
    }

    public synchronized String generateCourseId() {
        JSONArray courses = readCourses();
        int max = 0;
        for (Object o : courses) {
            JSONObject c = (JSONObject) o;
            try {
                max = Math.max(max, Integer.parseInt(c.optString("id", "0")));
            } catch (Exception ignored) {}
        }
        return String.valueOf(max + 1);
    }

    public synchronized String generateLessonId(JSONArray lessons) {
        int max = 0;
        for (Object o : lessons) {
            JSONObject l = (JSONObject) o;
            max = Math.max(max, l.optInt("id", 0));
        }
        return String.valueOf(max + 1);
    }

    /** QUIZ / LESSON COMPLETION **/

    public synchronized void markLessonCompleted(String studentId, String courseId, String lessonId) {
        JSONObject user = findUserById(studentId);
        if (user == null) return;

        JSONArray completions = user.optJSONArray("lessonCompletions");
        if (completions == null) completions = new JSONArray();

        JSONObject completion = new JSONObject();
        completion.put("courseId", courseId);
        completion.put("lessonId", lessonId);
        completions.put(completion);

        user.put("lessonCompletions", completions);
        updateUser(user);
    }

    public synchronized boolean isLessonCompleted(String studentId, String courseId, String lessonId) {
        JSONObject user = findUserById(studentId);
        if (user == null) return false;

        JSONArray completions = user.optJSONArray("lessonCompletions");
        if (completions == null) return false;

        for (Object o : completions) {
            JSONObject c = (JSONObject) o;
            if (courseId.equals(c.optString("courseId")) && lessonId.equals(c.optString("lessonId"))) {
                return true;
            }
        }
        return false;
    }

    public synchronized void recordQuizScore(String studentId, String quizId, double score) {
        JSONObject user = findUserById(studentId);
        if (user == null) return;

        JSONArray attempts = user.optJSONArray("quizAttempts");
        if (attempts == null) attempts = new JSONArray();

        JSONObject attempt = new JSONObject();
        attempt.put("quizId", quizId);
        attempt.put("score", score);
        attempts.put(attempt);

        user.put("quizAttempts", attempts);
        updateUser(user);
    }

    public synchronized Double getBestScoreForQuiz(String studentId, String quizId) {
        JSONObject user = findUserById(studentId);
        if (user == null) return null;

        JSONArray attempts = user.optJSONArray("quizAttempts");
        if (attempts == null) return null;

        double best = -1;
        for (Object o : attempts) {
            JSONObject a = (JSONObject) o;
            if (quizId.equals(a.optString("quizId"))) {
                best = Math.max(best, a.optDouble("score", 0));
            }
        }
        return best >= 0 ? best : null;
    }
}
