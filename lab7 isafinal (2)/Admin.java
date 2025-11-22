/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab8;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author carol
 */
public class Admin{ //inheritence fe haga ghalat
    private JSONObject adminData;
    
    public Admin(JSONObject adminData){
         this.adminData = adminData;
    }
    
    public Admin(String userId, String username, String email, String password){
        this.adminData = new JSONObject();
        adminData.put("id", userId);
        adminData.put("role", "admin");
        adminData.put("username", username);
        adminData.put("email", email);
        adminData.put("password", password);
    }
    
}
