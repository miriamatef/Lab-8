/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package skillforge;
import java.util.*;
import skillforge.Course;

/**
 *
 * @author Miriam
 */

public class CourseStats {
    private int courseId;
    private String title;
    private int enrolledCount;
    private double average;
    private List<LessonStats> lessons;

    public CourseStats(int courseId, String title, int enrolledCount, double average, List<LessonStats> lessons) {
        this.courseId = courseId;
        this.title = title;
        this.enrolledCount = enrolledCount;
        this.average = average;
        this.lessons = lessons;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public List<LessonStats> getLessons() {
        return lessons;
    }

    public void setLessons(List<LessonStats> lessons) {
        this.lessons = lessons;
    }

    
}