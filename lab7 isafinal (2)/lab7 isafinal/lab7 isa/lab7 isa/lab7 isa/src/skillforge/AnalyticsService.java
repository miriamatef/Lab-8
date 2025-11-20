/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;

import java.util.*;
import org.json.*;
import skillforge.StudentProgress;


/**
 *
 * @author Miriam
 */

public class AnalyticsService {
    private final JsonDatabaseManager db= JsonDatabaseManager.getInstance();
    
    
    public CourseStats calculateCourseStats (int courseId){
        JSONArray courses = db.readCourses();
        JSONArray users = db.readUsers();
        
        JSONObject object = findCourse (courses, courseId);
        if (object == null)
            throw new RuntimeException ("\nCourse with id "+courseId+" is not found!");
        
        String courseTitle = object.optString("title", "untitled");
        //optString("title", "Untitled") returns the string value if present 
        //otherwise returns default "Untitled". optString avoids exceptions on missing keys.
        
        JSONArray lessons = object.optJSONArray("lessons");
        //to get the lessons array from the course object.
        if (lessons == null) lessons = new JSONArray();
        
        List <LessonStats> lessonStats = new ArrayList<>();
        int enrolledCount = countEnrolledStudents(users, courseId);
        
        for (Object l : lessons) {
            JSONObject lessonObject = (JSONObject) l;

            int lessonId = lessonObject.optInt("lessonId", lessonObject.optInt("id", -1));
            //Tries to retrieve the lesson identifier. First reads lessonId property
            //if missing it tries id as fallback, if both missing returns -1.
            
            String lessonTitle = lessonObject.optString("title", "Lesson " + lessonId);
            //Reads title from lesson object or uses "Lesson " + lessonId if title missing.

            double avgScore = calculateLessonAverageScore(users, courseId, lessonId);
            double completionPercent = calculateLessonCompletion(users, courseId, lessonId, enrolledCount);

            lessonStats.add(new LessonStats(lessonId,lessonTitle,round(avgScore),round(completionPercent)));
        }
        
        double overallAvg = computeOverallAverage(lessonStats);

        
        return new CourseStats (courseId, courseTitle, enrolledCount, round(overallAvg), lessonStats);
    }
    
    //method that is looking for a course by id
    private JSONObject findCourse (JSONArray courses, int courseId){
        for (Object o :courses){
            JSONObject c = (JSONObject) o;
            if (c.optInt("courseId", c.optInt("id"))==courseId)
                return c;
        }
        return null;
    }
    
    //A method to know how many students are enrolled fel course bel id da
    private int countEnrolledStudents (JSONArray users, int courseID){
        int count=0;
        for (Object o:users){
            JSONObject u = (JSONObject) o;
            JSONArray a= u.optJSONArray("enrolledCourses");
            if (a == null)
                continue;
            for (Object c:a){
                if (((Number) c).intValue()==courseID){
                    count ++;
                    break;
                }
            }
        }
        return count;
    }
    
    
    //method to compute mean quiz percentage for a lesson across enrolled students.
    private double calculateLessonAverageScore (JSONArray users, int courseID, int lessonID){
        List<Double> scores= new ArrayList<>(); //Initialize empty scores list to collect percentage values.
        
        for (Object o : users) {
            JSONObject u = (JSONObject) o;

            JSONArray attempts = u.optJSONArray("quizAttempts");
            if (attempts == null) continue;

            for (Object raw : attempts) {
                JSONObject att = (JSONObject) raw;

                if (att.optInt("courseId") == courseID && att.optInt("lessonId") == lessonID) {
                    double score = att.optDouble("score", 0);
                    double max = att.optDouble("maxScore", 100);
                    if (max <= 0) 
                        max = 100; //If maxScore <= 0 fallback to 100 to avoid division by zero or negative normalization.

                    scores.add((score / max) * 100);
                }
            }
        }

        if (scores.isEmpty()) 
            return 0;

        double sum = 0;
        for (double s : scores) 
            sum += s;

        double avg= (sum/scores.size());
        return avg;
            
    }
    
    //this method calculates percentage of how many enrolled students completed this lesson using previously computed enrolledCount.
    private double calculateLessonCompletion (JSONArray users, int courseId, int lessonId, int enrolledCount){
        if (enrolledCount == 0)
            return 0;
        
        int completedCount=0;
        
        for (Object o:users){
            JSONObject user = (JSONObject)o;
            JSONArray array = user.optJSONArray("completedLessons");
            if (array == null)
                continue;
            
            for (Object raw:array){
                JSONObject completed = (JSONObject)raw;
                if ((completed.optInt("courseId")==courseId) && (completed.optInt("lessonId")==lessonId)){
                    completedCount++;
                    break;
                }
            }
        }
        
        double percentage = (completedCount/enrolledCount)*100;
        return percentage;
    }
    
    // a helper to trim values to two decimal places
    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
    
    private double computeOverallAverage(List<LessonStats> lessonStats){
        if (lessonStats.isEmpty())
            return 0;
        
        double sum=0;
        
        for (LessonStats l:lessonStats){
            sum+=l.getAverageScore();
        }
        
        double avg = (sum/lessonStats.size());
        return avg;
    }
    
    
    public StudentProgress computeStudentProgress(String studentId, int courseId) {
        JSONArray courses = db.readCourses();
        JSONArray users = db.readUsers();
        
        JSONObject student = findUser(users, studentId);
        if (student == null)
            throw new RuntimeException("Student with id " + studentId+ " is not found!");

        JSONObject course = findCourse(courses, courseId);
        if (course == null)
            throw new RuntimeException("Course with id " + courseId+ " is not found!");
        
        JSONArray lessons = course.optJSONArray("lessons");
        if (lessons == null) 
            lessons = new JSONArray();
        
        Map<Integer, Double> lessonScores = new HashMap<>();
        //map (lessonId -> best percentage for this student)
        
        int totalLessons = lessons.length();
        int completed = 0;
        
        for (Object o:lessons){
            JSONObject l = (JSONObject)o;
            int lessonId = l.optInt("lessonId");
            
            // get best quiz score
            double bestScore = getBestStudentScore(student, courseId, lessonId);
            lessonScores.put(lessonId, round(bestScore));

            if (studentCompletedLesson(student, courseId, lessonId))
                completed++;
            
        }
        
        double percentage = totalLessons == 0 ? 0 : (100.0 * completed / totalLessons);
        
        return new StudentProgress(studentId, student.optString("name", "studentName"), courseId, percentage, lessonScores);
    }
    
    private JSONObject findUser (JSONArray users, String studentId){
        for (Object o :users){
            JSONObject u = (JSONObject) o;
            if (studentId.equals(u.optString("id")))
                return u;
        }
        return null;
    }
    
    //a method that Computes the best (maximum) percentage the given student has achieved for a particular course + lesson across their quiz attempts.
    private double getBestStudentScore (JSONObject student, int courseId, int lessonId){
        JSONArray attempts = student.optJSONArray("quizAttempts");
        if (attempts == null) return 0;

        double best = 0;

        for (Object raw : attempts) {
            JSONObject a = (JSONObject) raw;

            if (a.optInt("courseId") == courseId &&
                    a.optInt("lessonId") == lessonId) {

                double score = a.optDouble("score", 0);
                double max = a.optDouble("maxScore", 100);
                double pct = (score / max) * 100;

                best = Math.max(best, pct);
            }
        }

        return best;
    }
    
    
    //this method checks if a student has a completedLessons entry matching the specified course and lesson
    private boolean studentCompletedLesson(JSONObject student, int courseId, int lessonId){
        JSONArray arr = student.optJSONArray("completedLessons");
        if (arr == null) return false;

        for (Object o : arr) {
            JSONObject cl = (JSONObject) o;
            if (cl.optInt("courseId") == courseId && cl.optInt("lessonId") == lessonId)
                return true;
        }

        return false;
    }
}
