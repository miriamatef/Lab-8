/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;

import org.json.*;
import java.util.HashSet;
import java.util.Set;

public class Progress {
    private JSONObject progressData;
    
    public Progress(JSONObject progressData) {
        this.progressData = progressData;
        if (!progressData.has("completedLessonIds")) {
            progressData.put("completedLessonIds", new JSONArray());
        }
    }
    
    public Progress(int courseId) {
        this.progressData = new JSONObject();
        progressData.put("courseId", courseId);
        progressData.put("completedLessonIds", new JSONArray());
    }
    
    public Progress() {
        this.progressData = new JSONObject();
        progressData.put("completedLessonIds", new JSONArray());
    }
    

    public JSONObject toJSON() {
        return progressData;
    }
    

    public int getCourseId() {
        return progressData.optInt("courseId", -1);
    }
    

    public void setCourseId(int courseId) {
        progressData.put("courseId", courseId);
    }
    

    public Set<Integer> getCompletedLessonIds() {
        Set<Integer> lessonIds = new HashSet<>();
        JSONArray lessonsArray = progressData.optJSONArray("completedLessonIds");
        if (lessonsArray != null) {
            for (Object obj : lessonsArray) {
                if (obj instanceof Integer) {
                    lessonIds.add((Integer) obj);
                } else if (obj instanceof String) {
                    try {
                        lessonIds.add(Integer.parseInt((String) obj));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return lessonIds;
    }
    
    public void setCompletedLessonIds(Set<Integer> completedLessonIds) {
        JSONArray lessonsArray = new JSONArray();
        for (Integer lessonId : completedLessonIds) {
            lessonsArray.put(lessonId);
        }
        progressData.put("completedLessonIds", lessonsArray);
    }
    

    public void markLessonCompleted(int lessonId) {
        JSONArray lessonsArray = progressData.optJSONArray("completedLessonIds");
        if (lessonsArray == null) {
            lessonsArray = new JSONArray();
            progressData.put("completedLessonIds", lessonsArray);
        }
        
        for (Object obj : lessonsArray) {
            if (obj instanceof Integer && (Integer) obj == lessonId) {
                return; 
            }
        }
        
        lessonsArray.put(lessonId);
    }
    
    public boolean isLessonCompleted(int lessonId) {
        JSONArray lessonsArray = progressData.optJSONArray("completedLessonIds");
        if (lessonsArray == null) return false;
        
        for (Object obj : lessonsArray) {
            if (obj instanceof Integer && (Integer) obj == lessonId) {
                return true;
            }
        }
        return false;
    }
    
    public void unmarkLessonCompleted(int lessonId) {
        JSONArray lessonsArray = progressData.optJSONArray("completedLessonIds");
        if (lessonsArray == null) return;
        
        JSONArray newArray = new JSONArray();
        for (Object obj : lessonsArray) {
            if (!(obj instanceof Integer && (Integer) obj == lessonId)) {
                newArray.put(obj);
            }
        }
        progressData.put("completedLessonIds", newArray);
    }
    
    public double getCompletionPercentage(int totalLessons) {
        if (totalLessons == 0) return 0.0;
        return (getCompletedLessonIds().size() * 100.0) / totalLessons;
    }
    
    @Override
    public String toString() {
        return "Progress for Course " + getCourseId() + 
               " - Completed: " + getCompletedLessonIds().size() + " lessons";
    }
}