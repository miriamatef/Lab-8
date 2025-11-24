/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;
import java.util.*;

/**
 *
 * @author Miriam
 */

public class StudentProgress {
    private String studentId;
    private String studentName;
    private int courseId;
    private double courseCompletionPercentage;
    private Map<Integer, Double> lessonScore;

    public StudentProgress(String studentId, String studentName, int courseId, double courseCompletionPercentage, Map<Integer, Double> lessonScore) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseCompletionPercentage = courseCompletionPercentage;
        this.lessonScore = lessonScore;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public double getCourseCompletionPercentage() {
        return courseCompletionPercentage;
    }

    public void setCourseCompletionPercentage(double courseCompletionPercentage) {
        this.courseCompletionPercentage = courseCompletionPercentage;
    }

    public Map<Integer, Double> getLessonScore() {
        return lessonScore;
    }

    public void setLessonScore(Map<Integer, Double> lessonScore) {
        this.lessonScore = lessonScore;
    }
    
    
}
