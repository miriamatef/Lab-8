package lab7.isa;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class QuizFrame extends JFrame {

    private final Quiz quiz;
    private final String courseId;
    private final String lessonId;
    private final String studentId;
    private final QuizService quizService;
    private final LessonService lessonService;

    private final List<ButtonGroup> groups = new ArrayList<>();

    public QuizFrame(Quiz quiz, String courseId, String lessonId, String studentId,
                     QuizService quizService, LessonService lessonService) {
        this.quiz = quiz;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.quizService = quizService;
        this.lessonService = lessonService;

        setTitle("Quiz - Lesson " + lessonId);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());

  
        JLabel titleLabel = new JLabel("Quiz for Lesson " + lessonId);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(titleLabel, BorderLayout.NORTH);

      
        JPanel questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (quiz == null || quiz.getQuestions().isEmpty()) {
            JLabel noQuizLabel = new JLabel("No quiz available for this lesson.");
            noQuizLabel.setFont(new Font("Arial", Font.BOLD, 16));
            questionsPanel.add(noQuizLabel);
        } else {
            int qIndex = 0;
            for (Question q : quiz.getQuestions()) {
                JPanel qPanel = new JPanel();
                qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.Y_AXIS));
                qPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(10, 0, 10, 0),
                        BorderFactory.createEtchedBorder()
                ));

                JLabel questionLabel = new JLabel("<html><b>Q" + (qIndex + 1) + ":</b> " + q.getText() + "</html>");
                questionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                qPanel.add(questionLabel);

                ButtonGroup bg = new ButtonGroup();
                groups.add(bg);

                List<String> opts = q.getOptions();
                for (int i = 0; i < opts.size(); i++) {
                    JRadioButton rb = new JRadioButton(opts.get(i));
                    rb.setActionCommand(String.valueOf(i));
                    rb.setFont(new Font("Arial", Font.PLAIN, 13));
                    bg.add(rb);
                    qPanel.add(rb);
                }

                questionsPanel.add(qPanel);
                qIndex++;
            }
        }

        JScrollPane scroll = new JScrollPane(questionsPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        main.add(scroll, BorderLayout.CENTER);

   
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitBtn = new JButton("Submit");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        submitBtn.addActionListener((ActionEvent e) -> onSubmit());
        bottom.add(submitBtn);
        main.add(bottom, BorderLayout.SOUTH);

        this.setContentPane(main);
    }

    private void onSubmit() {
        List<Integer> answers = new ArrayList<>();
        for (ButtonGroup bg : groups) {
            String sel = null;
            for (Enumeration<AbstractButton> en = bg.getElements(); en.hasMoreElements();) {
                AbstractButton b = en.nextElement();
                if (b.isSelected()) {
                    sel = b.getActionCommand();
                    break;
                }
            }
            answers.add(sel == null ? -1 : Integer.parseInt(sel));
        }

     
        boolean allUnanswered = answers.stream().allMatch(a -> a == -1);
        if (allUnanswered) {
            JOptionPane.showMessageDialog(this, "Please answer at least one question before submitting.",
                    "No Answers", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int score = quizService.gradeQuiz(quiz, answers);
        boolean passed = quizService.isPassed(quiz, score);

     
        quizService.saveQuizResult(studentId, courseId, lessonId, score, passed);
        quizService.incrementAttemptCount(studentId, courseId, lessonId);

        
        if (passed) {
            lessonService.markLessonCompleted(
                    Integer.parseInt(courseId),
                    Integer.parseInt(lessonId),
                    studentId,
                    score
            );
        }

      
        StringBuilder feedback = new StringBuilder();
        feedback.append("Score: ").append(score).append("%\n");
        feedback.append("Passing Score: ").append(quiz.getPassingScore()).append("%\n");
        feedback.append(passed ? "Status: PASSED\n\n" : "Status: FAILED\n\n");
        feedback.append("Correct answers:\n");

        int i = 0;
        for (Question q : quiz.getQuestions()) {
            int correctIndex = q.getCorrectIndex();
            String correctText = q.getOptions().get(correctIndex);
            feedback.append("Q").append(i + 1)
                    .append(": ").append(correctText)
                    .append(" (option index = ").append(correctIndex).append(")\n");
            i++;
        }

        JOptionPane.showMessageDialog(this, feedback.toString(), "Quiz Result", JOptionPane.INFORMATION_MESSAGE);

        if (passed) {
            JOptionPane.showMessageDialog(this, "You passed! You may access the next lesson.",
                    "Passed", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } else {
            int attempts = quizService.getAttemptCount(studentId, courseId, lessonId);
            int retry = JOptionPane.showConfirmDialog(this,
                    "You did not pass. Attempts so far: " + attempts + ". Do you want to try again?",
                    "Not Passed", JOptionPane.YES_NO_OPTION);

            if (retry == JOptionPane.YES_OPTION) {
                QuizFrame retryFrame = new QuizFrame(quiz, courseId, lessonId, studentId, quizService, lessonService);
                retryFrame.setVisible(true);
            }
            this.dispose();
        }
    }
}
