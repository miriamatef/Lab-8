/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package skillforge;

import skillforge.Lesson;

/**
 *
 * @author Miriam
 */

public class LessonStats {
    private int lessonId;
    private String lessonTitle;
    private double averageScore; // percent
    private double completionPercent; // percent

    public LessonStats(int lessonId, String lessonTitle, double averageScore, double completionPercent) {
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.averageScore = averageScore;
        this.completionPercent = completionPercent;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(double completionPercent) {
        this.completionPercent = completionPercent;
    }
    
}
