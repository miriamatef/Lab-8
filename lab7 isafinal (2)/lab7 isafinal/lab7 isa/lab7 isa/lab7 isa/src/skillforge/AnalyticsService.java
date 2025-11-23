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

    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();

    public CourseStats calculateCourseStats(int courseId) {
        JSONArray courses = db.readCourses();
        JSONArray users = db.readUsers();

        JSONObject object = findCourse(courses, courseId);
        if (object == null) {
            throw new RuntimeException("\nCourse with id " + courseId + " is not found!");
        }

        String courseTitle = object.optString("title", "untitled");
        //optString("title", "Untitled") returns the string value if present 
        //otherwise returns default "Untitled". optString avoids exceptions on missing keys.

        JSONArray lessons = object.optJSONArray("lessons");
        //to get the lessons array from the course object.
        if (lessons == null) {
            lessons = new JSONArray();
        }

        List<LessonStats> lessonStats = new ArrayList<>();
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

            lessonStats.add(new LessonStats(lessonId, lessonTitle, round(avgScore), round(completionPercent)));
        }

        double overallAvg = computeOverallAverage(lessonStats);

        return new CourseStats(courseId, courseTitle, enrolledCount, round(overallAvg), lessonStats);
    }

    //method that is looking for a course by id
    private JSONObject findCourse(JSONArray courses, int courseId) {
        for (Object o : courses) {
            JSONObject c = (JSONObject) o;
            if (c.optInt("courseId", c.optInt("id")) == courseId) {
                return c;
            }
        }
        return null;
    }

    //A method to know how many students are enrolled fel course bel id da
    private int countEnrolledStudents(JSONArray users, int courseID) {
        int count = 0;
        for (Object o : users) {
            JSONObject u = (JSONObject) o;
            JSONArray a = u.optJSONArray("enrolledCourses");
            if (a == null) {
                continue;
            }
            for (Object c : a) {
                if (((Number) c).intValue() == courseID) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private double calculateLessonAverageScore(JSONArray users, int courseID, int lessonID) {
    List<Double> scores = new ArrayList<>();

    for (Object o : users) {
        JSONObject u = (JSONObject) o;

        JSONObject quizAttempts = u.optJSONObject("quizAttempts");
        if (quizAttempts == null) continue;

        JSONObject courseObj = quizAttempts.optJSONObject(String.valueOf(courseID));
        if (courseObj == null) continue;

        JSONObject lessonObj = courseObj.optJSONObject(String.valueOf(lessonID));
        if (lessonObj == null) continue;

        double score = lessonObj.optDouble("score", 0);
        scores.add(score);
    }

    if (scores.isEmpty()) return 0;

    double sum = 0;
    for (double s : scores) sum += s;
    return sum / scores.size();
}
    //this method calculates percentage of how many enrolled students completed this lesson using previously computed enrolledCount.
    private double calculateLessonCompletion(JSONArray users, int courseId, int lessonId, int enrolledCount) {
        if (enrolledCount == 0) {
            return 0.0;
        }

        int completedCount = 0;

        for (Object o : users) {
            JSONObject user = (JSONObject) o;
            // NEW: progress is an array that contains objects { courseId, completedLessonIds: [...] }
            JSONArray progress = user.optJSONArray("progress");
            if (progress == null) {
                continue;
            }

            for (Object p : progress) {
                JSONObject prog = (JSONObject) p;
                if (prog.optInt("courseId", -1) != courseId) {
                    continue;
                }
                JSONArray completedIds = prog.optJSONArray("completedLessonIds");
                if (completedIds == null) {
                    continue;
                }

                // check if this student completed this lesson
                for (Object id : completedIds) {
                    if (((Number) id).intValue() == lessonId) {
                        completedCount++;
                        break;
                    }
                }
                // once we examined this course entry for this user, no need to check other progress entries
                break;
            }
        }

        // use floating point arithmetic to avoid integer division
        return (100.0 * completedCount) / enrolledCount;
    }

    // a helper to trim values to two decimal places
    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private double computeOverallAverage(List<LessonStats> lessonStats) {
        if (lessonStats.isEmpty()) {
            return 0;
        }

        double sum = 0;

        for (LessonStats l : lessonStats) {
            sum += l.getAverageScore();
        }

        double avg = (sum / lessonStats.size());
        return avg;
    }

    public StudentProgress computeStudentProgress(String studentId, int courseId) {
        JSONArray courses = db.readCourses();
        JSONArray users = db.readUsers();

        JSONObject student = findUser(users, studentId);
        if (student == null) {
            throw new RuntimeException("Student with id " + studentId + " is not found!");
        }

        JSONObject course = findCourse(courses, courseId);
        if (course == null) {
            throw new RuntimeException("Course with id " + courseId + " is not found!");
        }

        JSONArray lessons = course.optJSONArray("lessons");
        if (lessons == null) {
            lessons = new JSONArray();
        }

        Map<Integer, Double> lessonScores = new HashMap<>();
        //map (lessonId -> best percentage for this student)

        int totalLessons = lessons.length();
        int completed = 0;

        for (Object o : lessons) {
            JSONObject l = (JSONObject) o;
            int lessonId = l.optInt("lessonId");

            // get best quiz score
            double bestScore = getBestStudentScore(student, courseId, lessonId);
            lessonScores.put(lessonId, round(bestScore));

            if (studentCompletedLesson(student, courseId, lessonId)) {
                completed++;
            }

        }

        double percentage = totalLessons == 0 ? 0 : (100.0 * completed / totalLessons);

        return new StudentProgress(studentId, student.optString("name", "studentName"), courseId, percentage, lessonScores);
    }

    private JSONObject findUser(JSONArray users, String studentId) {
        for (Object o : users) {
            JSONObject u = (JSONObject) o;
            if (studentId.equals(u.optString("id"))) {
                return u;
            }
        }
        return null;
    }

    //a method that Computes the best (maximum) percentage the given student has achieved for a particular course + lesson across their quiz attempts.
    private double getBestStudentScore(JSONObject student, int courseId, int lessonId) {
        
    JSONObject quizAttempts = student.optJSONObject("quizAttempts");
    if (quizAttempts == null) return 0;

    JSONObject courseObj = quizAttempts.optJSONObject(String.valueOf(courseId));
    if (courseObj == null) return 0;

    JSONObject lessonObj = courseObj.optJSONObject(String.valueOf(lessonId));
    if (lessonObj == null) return 0;

    return lessonObj.optDouble("score", 0); // already stored as percentage
}

    //this method checks if a student has a completedLessons entry matching the specified course and lesson
    private boolean studentCompletedLesson(JSONObject student, int courseId, int lessonId) {
        JSONArray progress = student.optJSONArray("progress");
        if (progress == null) {
            return false;
        }

        for (Object o : progress) {
            JSONObject p = (JSONObject) o;
            if (p.optInt("courseId", -1) != courseId) {
                continue;
            }
            JSONArray completed = p.optJSONArray("completedLessonIds");
            if (completed == null) {
                continue;
            }
            for (Object id : completed) {
                if (((Number) id).intValue() == lessonId) {
                    return true;
                }
            }
            // found the course entry but didn't find lesson -> not completed
            return false;
        }
        return false;
    }

public boolean markLessonCompleted(int courseId, int lessonId, String studentId, int score) {
    CourseService courseService = new CourseService(db);
    Quiz quiz = courseService.getQuiz(courseId, lessonId);

    if (quiz == null) return false;

    int maxScore = quiz.getQuestions().size();

    // Record quiz attempt
    recordQuizAttempt(studentId, courseId, lessonId, score, maxScore);

    // Mark completed if passing
    if (score >= quiz.getPassingScore()) {
        JSONArray users = db.readUsers();

        for (int i = 0; i < users.length(); i++) {
            JSONObject u = users.getJSONObject(i);
            if (!studentId.equals(u.optString("id", ""))) continue;

            JSONArray progress = u.optJSONArray("progress");
            if (progress == null) continue;

            boolean courseFound = false;
            for (Object o : progress) {
                JSONObject p = (JSONObject) o;
                if (p.optInt("courseId") == courseId) {
                    courseFound = true;
                    JSONArray completed = p.optJSONArray("completedLessonIds");
                    if (completed == null) {
                        completed = new JSONArray();
                        p.put("completedLessonIds", completed);
                    }
                    if (!containsInt(completed, lessonId)) {
                        completed.put(lessonId);
                        db.writeUsers(users);
                        return true;
                    }
                }
            }

            if (!courseFound) {
                JSONObject newProgress = new JSONObject();
                newProgress.put("courseId", courseId);
                JSONArray completed = new JSONArray();
                completed.put(lessonId);
                newProgress.put("completedLessonIds", completed);
                progress.put(newProgress);
                db.writeUsers(users);
                return true;
            }
        }
    }

    return false;
}

private boolean containsInt(JSONArray arr, int val) {
    for (Object o : arr) {
        if (((Number) o).intValue() == val) return true;
    }
    return false;
}

private void recordQuizAttempt(String studentId, int courseId, int lessonId, int score, int maxScore) {
    JSONArray users = db.readUsers();
    for (int i = 0; i < users.length(); i++) {
        JSONObject u = users.getJSONObject(i);
        if (!studentId.equals(u.optString("id", ""))) continue;

        JSONObject quizAttempts = u.optJSONObject("quizAttempts");
        if (quizAttempts == null) {
            quizAttempts = new JSONObject();
            u.put("quizAttempts", quizAttempts);
        }

        JSONObject courseObj = quizAttempts.optJSONObject(String.valueOf(courseId));
        if (courseObj == null) {
            courseObj = new JSONObject();
            quizAttempts.put(String.valueOf(courseId), courseObj);
        }

        JSONObject lessonObj = courseObj.optJSONObject(String.valueOf(lessonId));
        if (lessonObj == null) {
            lessonObj = new JSONObject();
        }

        lessonObj.put("score", score);
        lessonObj.put("maxScore", maxScore);
        courseObj.put(String.valueOf(lessonId), lessonObj);

        quizAttempts.put(String.valueOf(courseId), courseObj);
        u.put("quizAttempts", quizAttempts);

        db.writeUsers(users);
        return;
    }
}
}
