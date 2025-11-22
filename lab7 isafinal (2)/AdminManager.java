/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab8;

import lab7.isa.JsonDatabaseManager;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author carol
 */
public class AdminManager {
    private final JsonDatabaseManager db;

    public AdminManager(JsonDatabaseManager db) { 
        this.db = db; 
    }
    
    public JSONObject courseReview(String courseName) {
        JSONArray courses = db.readCourses();
        for (int i = 0; i < courses.length(); i++) {
            JSONObject course = courses.getJSONObject(i);
            if (course.getString("title").equalsIgnoreCase(courseName)) {
                return course; 
            }
        }
        return null; 
    }
    
    public boolean approveCourse(String courseName){
        JSONArray courses = db.readCourses(); 
        for (int i = 0; i < courses.length(); i++) {
            JSONObject course = courses.getJSONObject(i);
            if (course.getString("title").equalsIgnoreCase(courseName)) {
                course.put("status", "APPROVED");
                db.writeCourses(courses); 
                return true;
            }
        }
        return false;
    }
    
    public boolean declineCourse(String courseName){
      JSONArray courses = db.readCourses(); 
        for (int i = 0; i < courses.length(); i++) {
            JSONObject course = courses.getJSONObject(i);
            if (course.getString("title").equalsIgnoreCase(courseName)) {
                course.put("status", "DECLINED");
                db.writeCourses(courses); 
                return true;
            }
        }
        return false;
    }
    
    public JSONArray getCourses() {
        return db.readCourses();
    }

}
