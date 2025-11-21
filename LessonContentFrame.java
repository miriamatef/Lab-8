package lab7.isa;

import javax.swing.*;
import java.awt.*;

public class LessonContentFrame extends JFrame {
    private final Course course;
    private final Lesson lesson;
    private final String studentId;
    private final QuizService quizService;

    private JTextArea contentArea;
private final LessonService lessonService;


public LessonContentFrame(Course course, Lesson lesson, String studentId,
                          QuizService quizService, LessonService lessonService) {
    this.course = course;
    this.lesson = lesson;
    this.studentId = studentId;
    this.quizService = quizService;
    this.lessonService = lessonService;
    initUI();
}


    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());

        contentArea = new JTextArea();
        contentArea.setText(lesson.getContent()); 
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(contentArea);
        main.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton takeQuizBtn = new JButton("Take Quiz");
        JButton nextLessonBtn = new JButton("Next Lesson");
boolean passed = quizService.hasPassedLesson(
    studentId,
    String.valueOf(course.getCourseId()),
    String.valueOf(lesson.getLessonId())
);
if (!passed) {
    nextLessonBtn.setEnabled(false);
}

        bottom.add(takeQuizBtn);
        bottom.add(nextLessonBtn);

        takeQuizBtn.addActionListener(e -> {
    Quiz quiz = lesson.getQuiz();
    if (quiz == null || quiz.getQuestions().isEmpty()) {
        JOptionPane.showMessageDialog(this, "No quiz available for this lesson.");
        return;
    }
    QuizFrame quizFrame = new QuizFrame(
            quiz,
            String.valueOf(course.getCourseId()),
            String.valueOf(lesson.getLessonId()),
            studentId,
            quizService,
            lessonService  
    );
    quizFrame.setVisible(true);
});


      
        nextLessonBtn.addActionListener(e -> {
            int currentIndex = course.getLessons().indexOf(lesson);
            if (currentIndex < course.getLessons().size() - 1) {
               
                boolean qpassed = quizService.hasPassedLesson(
                        studentId,
                        String.valueOf(course.getCourseId()),
                        String.valueOf(lesson.getLessonId())
                );
                if (!passed) {
                    JOptionPane.showMessageDialog(this,
                            "You must pass the quiz for this lesson before accessing the next one.",
                            "Access Denied",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Lesson nextLesson = course.getLessons().get(currentIndex + 1);
                LessonContentFrame nextFrame = new LessonContentFrame(course, nextLesson, studentId, quizService,lessonService);
                nextFrame.setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No more lessons in this course.");
            }
        });

        main.add(bottom, BorderLayout.SOUTH);
        setContentPane(main);
    }
}
