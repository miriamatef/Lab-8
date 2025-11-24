/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;

import org.json.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mariam Elshamy
 */
public class UsersJsonManager {
    private static final Logger logger = Logger.getLogger(UsersJsonManager.class.getName());
    private final String filename;
    
  
    public UsersJsonManager(String filename) {
        this.filename = filename;
        ensureFileExists();
    }
    
  
    private void ensureFileExists() {
        File file = new File(filename);
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("[]"); 
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create file: " + filename, e);
            }
        }
    }
    

    private JSONArray readUsers() {
        JSONArray usersArray = new JSONArray();
        File file = new File(filename);
        if (!file.exists()) { 
            return usersArray;
        }
        try (FileReader reader = new FileReader(file)) {
            if (file.length() == 0) {
                return usersArray;
            }
            usersArray = new JSONArray(new JSONTokener(reader));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading users", e);
        }
        return usersArray;
    }
    

    private void writeUsers(JSONArray users) {
        try (FileWriter file = new FileWriter(filename)) {
            file.write(users.toString(4)); 
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error writing users", e);
        }
    }
    
   
    public synchronized boolean emailExists(String email) {
        JSONArray users = readUsers();
        for (Object obj : users) {
            JSONObject user = (JSONObject) obj;
            String userEmail = user.optString("email", null);
            if (userEmail != null && email.equalsIgnoreCase(userEmail)) {
                return true;
            }
        }
        return false;
    }
    

    public synchronized boolean idExists(String id) {
        JSONArray users = readUsers();
        for (Object obj : users) {
            JSONObject user = (JSONObject) obj;
            String userId = user.optString("id", null);
            if (id.equals(userId)) {
                return true;
            }
        }
        return false;
    }
    
  
    public synchronized String generateUniqueUserId() {
        JSONArray users = readUsers();
        Set<String> existingIds = new HashSet<>();
        
        for (Object obj : users) {
            JSONObject user = (JSONObject) obj;
            String id = user.optString("id", null);
            if (id != null) {
                existingIds.add(id);
            }
        }
        
        String newId;
        Random rand = new Random();
        do {
            newId = "U" + (1000 + rand.nextInt(9000));
        } while (existingIds.contains(newId));
        
        return newId;
    }
    
  
    public synchronized void addUser(JSONObject userObj) {
        JSONArray users = readUsers();
        String id = userObj.optString("id", null);
        
        if (id == null) {
            throw new IllegalArgumentException("User must have an id field");
        }

        for (Object obj : users) {
            JSONObject user = (JSONObject) obj;
            if (id.equals(user.optString("id", null))) {
                throw new IllegalArgumentException("Duplicate userId");
            }
        }
        
        users.put(userObj);
        writeUsers(users);
    }
    

    public synchronized Optional<JSONObject> findByEmail(String email) {
        JSONArray users = readUsers();
        for (Object obj : users) {
            JSONObject user = (JSONObject) obj;
            String userEmail = user.optString("email", null);
            if (userEmail != null && email.equalsIgnoreCase(userEmail)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
    
    
    public synchronized void updateUser(JSONObject userObj) {
        JSONArray users = readUsers();
        String id = userObj.optString("id", null);
        
        if (id == null) {
            throw new IllegalArgumentException("User must have an id field");
        }
        
        boolean found = false;
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (id.equals(user.optString("id", null))) {
                users.put(i, userObj); 
                found = true;
                break;
            }
        }
        
        if (!found) {
            throw new IllegalArgumentException("User id not found");
        }
        
        writeUsers(users);
    }
    
    public synchronized List<JSONObject> getAllUsers() {
        JSONArray users = readUsers();
        List<JSONObject> userList = new ArrayList<>();
        for (Object obj : users) {
            userList.add((JSONObject) obj);
        }
        return userList;
    }
}
