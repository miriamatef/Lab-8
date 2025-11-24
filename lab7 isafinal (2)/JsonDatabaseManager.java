package skillforge;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonDatabaseManager {
    private static JsonDatabaseManager instance;
    private static final Logger logger = Logger.getLogger(JsonDatabaseManager.class.getName());

    private final File usersFile = new File("users.json");
    private final File coursesFile = new File("courses.json");

    public JsonDatabaseManager() {
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

    public synchronized JSONArray readUsers() {
        return readArrayFromFile(usersFile);
    }

    public synchronized JSONArray readCourses() {
        return readArrayFromFile(coursesFile);
    }

    private JSONArray readArrayFromFile(File f) {
        if (!f.exists()) return new JSONArray();
        try {
            if (f.length() == 0) return new JSONArray();
            try (FileReader reader = new FileReader(f)) {
                return new JSONArray(new JSONTokener(reader));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading " + f.getName(), e);
            return new JSONArray();
        }
    }

    public synchronized void writeUsers(JSONArray users) {
        writeArrayToFile(usersFile, users);
    }

    public synchronized void writeCourses(JSONArray courses) {
        writeArrayToFile(coursesFile, courses);
    }

    private void writeArrayToFile(File f, JSONArray arr) {
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(arr.toString(4)); // pretty-print JSON
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing " + f.getName(), e);
        }
    }

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

    public synchronized int generateCourseId() {
        JSONArray courses = readCourses();
        int max = 0;
        for (Object o : courses) {
            JSONObject c = (JSONObject) o;
            max = Math.max(max, c.optInt("id", 0));
        }
        return max + 1;
    }

    public synchronized int generateLessonId(JSONArray lessons) {
        int max = 0;
        for (Object o : lessons) {
            JSONObject l = (JSONObject) o;
            max = Math.max(max, l.optInt("id", 0));
        }
        return max + 1;
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

}
