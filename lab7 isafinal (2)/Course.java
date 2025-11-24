package skillforge;

import java.util.*;

public class Course {
    private int courseId;
    private String title;
    private String description;
    private String instructorId;
    private String status;
    private List<Lesson> lessons;
    private List<String> students; // stores student IDs

    public Course(int courseId, String title, String description, String instructorId,
                  List<Lesson> lessons, List<String> students, String status) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.lessons = lessons == null ? new ArrayList<>() : lessons;
        this.students = students == null ? new ArrayList<>() : students;
        this.status = status == null ? "Pending" : status;
    }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public List<Lesson> getLessons() { return lessons; }
    public void setLessons(List<Lesson> lessons) { this.lessons = lessons; }

    public List<String> getStudents() { return students; }
    public void setStudents(List<String> students) { this.students = students; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void addLesson(Lesson lesson) { lessons.add(lesson); }

    public void removeLesson(int lessonId) { 
        lessons.removeIf(l -> l.getLessonId() == lessonId); 
    }

    public void enrollStudent(String studentId) { 
        if (!students.contains(studentId)) students.add(studentId); 
    }

    
    public List<String> getStudentDisplayList(UserService userService) {
        List<String> display = new ArrayList<>();
        for (String id : students) {
            String name = userService.getUsernameById(id); // implement in UserService
            display.add(name != null ? name + " (" + id + ")" : id);
        }
        return display;
    }
    
    public Lesson getLessonById(int lessonId) {
    if (lessons == null) return null;
    for (Lesson l : lessons) {
        if (l.getLessonId() == lessonId) return l;
    }
    return null;
}
public Quiz getQuiz(int lessonId) {
    Lesson lesson = getLessonById(lessonId);
    return (lesson != null) ? lesson.getQuiz() : null;
}

public void setQuiz(int lessonId, Quiz quiz) {
    Lesson lesson = getLessonById(lessonId);
    if (lesson != null) {
        lesson.setQuiz(quiz);
    }
}

    @Override
    public String toString() { 
        return title; 
    }
}
