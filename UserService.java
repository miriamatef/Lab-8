package skillforge;

import java.security.MessageDigest;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserService {
    public final JsonDatabaseManager db;

    public UserService(JsonDatabaseManager db) {
        this.db = db;
    }

    
    public synchronized boolean signup(String role, String username, String email, String password) {
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            role == null || role.trim().isEmpty()) return false;

        JSONArray users = db.readUsers();
        for (Object o : users) {
            JSONObject u = (JSONObject) o;
            if (u.getString("username").equalsIgnoreCase(username) ||
                u.getString("email").equalsIgnoreCase(email)) return false;
        }
        String hashedPassword = hashPassword(password);
        JSONObject newUser = new JSONObject();
        newUser.put("id", db.generateUserId());
        newUser.put("role", role);
        newUser.put("username", username);
        newUser.put("email", email);
        newUser.put("password", hashedPassword);
        newUser.put("loggedIn", false);

        users.put(newUser);
        db.writeUsers(users);
        return true;
    }

    public JSONObject loginByEmail(String email, String password) {
    JSONArray users = db.readUsers();
    for (int i = 0; i < users.length(); i++) {
        JSONObject user = users.getJSONObject(i);
        if (user.getString("email").equalsIgnoreCase(email) &&
            user.getString("password").equals(hashPassword(password))) {
            return user; 
        }
    }
    return null;
    }


    public synchronized void logout(String email) {
        JSONArray users = db.readUsers();
        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (u.getString("email").equalsIgnoreCase(email)) {
                u.put("loggedIn", false);
                break;
            }
        }
        db.writeUsers(users);
    }

  
    public synchronized JSONObject getLoggedInUser() {
        JSONArray users = db.readUsers();
        for (Object o : users) {
            JSONObject u = (JSONObject) o;
            if (u.optBoolean("loggedIn", false)) return u;
        }
        return null;
    }
    
    public String getUsernameById(String userId) {
    JSONArray users = db.readUsers();
    for (Object o : users) {
        JSONObject u = (JSONObject) o;
        if (userId.equals(u.optString("id", ""))) {
            return u.optString("username", null);
        }
    }
    return null;
}
private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
