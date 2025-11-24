package skillforge;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuizManagementFrame extends JFrame {
    private final Course course;
    private final QuizService quizService;

    private JComboBox<Lesson> lessonComboBox;
    private JTextField questionField;
    private JTextField option1Field, option2Field, option3Field, option4Field;
    private JSpinner correctIndexSpinner;
  


    public QuizManagementFrame(Course course, JsonDatabaseManager db) {
        this.course = course;
        this.quizService = new QuizService(db);


        setTitle("Manage Quizzes - " + course.getTitle());
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        setVisible(true);
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        lessonComboBox = new JComboBox<>(course.getLessons().toArray(new Lesson[0]));
        panel.add(new JLabel("Select Lesson:"));
        panel.add(lessonComboBox);

        questionField = new JTextField();
        panel.add(new JLabel("Question Text:"));
        panel.add(questionField);

        option1Field = new JTextField();
        option2Field = new JTextField();
        option3Field = new JTextField();
        option4Field = new JTextField();
        panel.add(new JLabel("Option 1:")); panel.add(option1Field);
        panel.add(new JLabel("Option 2:")); panel.add(option2Field);
        panel.add(new JLabel("Option 3:")); panel.add(option3Field);
        panel.add(new JLabel("Option 4:")); panel.add(option4Field);

        correctIndexSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 3, 1));
        panel.add(new JLabel("Correct Option Index (0–3):"));
        panel.add(correctIndexSpinner);

      
        JButton addBtn = new JButton("Add Question");
        addBtn.addActionListener(e -> addQuestion());
        panel.add(addBtn);

        setContentPane(panel);
    }

    private void addQuestion() {
        Lesson lesson = (Lesson) lessonComboBox.getSelectedItem();
        if (lesson == null) {
            JOptionPane.showMessageDialog(this, "Select a lesson first.");
            return;
        }

        String text = questionField.getText();
        List<String> options = new ArrayList<>();
        options.add(option1Field.getText());
        options.add(option2Field.getText());
        options.add(option3Field.getText());
        options.add(option4Field.getText());

        int correctIndex = (int) correctIndexSpinner.getValue();

        Quiz quiz = lesson.getQuiz();
        if (quiz == null) {
            quiz = new Quiz();
            quiz.setLessonId(String.valueOf(lesson.getLessonId()));
            lesson.setQuiz(quiz);
        }
        lesson.setQuiz(quiz);
    quizService.addQuestionToQuiz(quiz, text, options, correctIndex);
    quizService.saveQuiz(
    String.valueOf(course.getCourseId()),
    String.valueOf(lesson.getLessonId()),
    quiz
);

 
        JOptionPane.showMessageDialog(this, "Question added successfully!");
    }
}