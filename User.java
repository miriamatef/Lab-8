package skillforge;

import org.json.*;

public abstract class User {
    private final JSONObject userData;

    public User(JSONObject userData) { this.userData = userData; }
    public JSONObject toJSON() { return userData; }
    public String getId() { return userData.optString("id",""); }
    public String getRole() { return userData.optString("role",""); }
    public String getUsername() { return userData.optString("username",""); }
    public String getEmail() { return userData.optString("email",""); }
}
