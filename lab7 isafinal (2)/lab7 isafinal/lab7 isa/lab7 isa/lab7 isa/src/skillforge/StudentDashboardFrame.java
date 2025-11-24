package skillforge;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudentDashboardFrame extends JFrame {
    private final String studentId;
    private final StudentManager studentManager;
    private final UserService userService;
    private JList<String> coursesList;
    private DefaultListModel<String> coursesListModel;
    private JList<String> lessonsList;
    private DefaultListModel<String> lessonsListModel;
    private JTextArea lessonContentArea;
    private JButton enrollButton;
    private JButton markCompleteButton;
    private final LoginFrame loginFrame;
    private JProgressBar progressBar;
    private JLabel progressLabel;

    private List<Course> availableCourses;
    
    private final QuizService quizService;
    private final LessonService lessonService;
    private JButton takeQuizButton;

    
    public StudentDashboardFrame(String studentId, JFrame loginFrame,
                                 QuizService quizService, LessonService lessonService) {
        super("Student Dashboard - " + studentId);//here
        this.studentId = studentId;
        this.studentManager = new StudentManager(JsonDatabaseManager.getInstance());
        this.userService = new UserService(JsonDatabaseManager.getInstance());
        this.loginFrame = (LoginFrame) loginFrame;
        this.quizService = quizService;
        this.lessonService = lessonService;

        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        loadCourses();
        addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowActivated(java.awt.event.WindowEvent e) {
            loadCourses(); // reload courses whenever the window regains focus
        }
    });
        setVisible(true);
    }

    public StudentDashboardFrame(String studentId, LoginFrame loginFrame) {
        this(studentId, loginFrame,
             new QuizService(JsonDatabaseManager.getInstance()),
             new LessonService(JsonDatabaseManager.getInstance()));
    }

    private void initUI() {
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            loginFrame.setVisible(true);
            dispose();
        });
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(logoutBtn);
        add(topBar, BorderLayout.NORTH);

        
        coursesListModel = new DefaultListModel<>();
        coursesList = new JList<>(coursesListModel);
        coursesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane coursesScroll = new JScrollPane(coursesList);
        coursesScroll.setPreferredSize(new Dimension(300, 0));

        enrollButton = new JButton("Enroll");
        enrollButton.addActionListener(e -> onEnrollClicked());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("Available Courses", JLabel.CENTER), BorderLayout.NORTH);
        leftPanel.add(coursesScroll, BorderLayout.CENTER);
        leftPanel.add(enrollButton, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(300, 0));

      
        lessonsListModel = new DefaultListModel<>();
        lessonsList = new JList<>(lessonsListModel);
        lessonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane lessonsScroll = new JScrollPane(lessonsList);

        markCompleteButton = new JButton("Mark Completed");
        markCompleteButton.addActionListener(e -> onMarkCompletedClicked());

        takeQuizButton = new JButton("Take Quiz");
        takeQuizButton.addActionListener(e -> onTakeQuizClicked());

        JPanel lessonButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lessonButtons.add(markCompleteButton);
        lessonButtons.add(takeQuizButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("Lessons", JLabel.CENTER), BorderLayout.NORTH);
        centerPanel.add(lessonsScroll, BorderLayout.CENTER);
        centerPanel.add(lessonButtons, BorderLayout.SOUTH);

       
        lessonContentArea = new JTextArea();
        lessonContentArea.setEditable(false);
        lessonContentArea.setLineWrap(true);
        lessonContentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(lessonContentArea);
        contentScroll.setPreferredSize(new Dimension(350, 0));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel("Lesson Content", JLabel.CENTER), BorderLayout.NORTH);
        rightPanel.add(contentScroll, BorderLayout.CENTER);

      
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressLabel = new JLabel("Progress: 0% (0/0 lessons)", JLabel.CENTER);

        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(progressPanel, BorderLayout.SOUTH);

       
        coursesList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int idx = coursesList.getSelectedIndex();
            if (idx < 0 || availableCourses == null) return;
            Course c = availableCourses.get(idx);
            lessonsListModel.clear();
            for (Lesson l : c.getLessons()) {
                lessonsListModel.addElement(l.getLessonId() + " — " + l.getTitle());
            }
            lessonContentArea.setText("");
            updateProgress(c);
        });

        lessonsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int cidx = coursesList.getSelectedIndex();
            int lidx = lessonsList.getSelectedIndex();
            if (cidx < 0 || lidx < 0 || availableCourses == null) return;
            Lesson l = availableCourses.get(cidx).getLessons().get(lidx);
            lessonContentArea.setText(l.getContent());
        });
    }

    private void onEnrollClicked() {
        int idx = coursesList.getSelectedIndex();
        if (idx < 0 || availableCourses == null) {
            JOptionPane.showMessageDialog(this, "Select a course first");
            return;
        }
        int courseId = availableCourses.get(idx).getCourseId();
        boolean ok = studentManager.enroll(studentId, courseId);
        JOptionPane.showMessageDialog(this, ok ? "Enrolled successfully" : "Enroll failed (maybe already enrolled)");
        loadCourses();
    }

    private void onMarkCompletedClicked() {
        int cidx = coursesList.getSelectedIndex();
        int lidx = lessonsList.getSelectedIndex();
        if (cidx < 0 || lidx < 0 || availableCourses == null) {
            JOptionPane.showMessageDialog(this, "Select course and lesson first");
            return;
        }
        int courseId = availableCourses.get(cidx).getCourseId();


Lesson lesson = lessonService.getLessons(courseId).get(lidx);

Quiz quiz = lesson.getQuiz();
if (quiz == null) {
    JOptionPane.showMessageDialog(this, "This lesson has no quiz. You cannot mark it completed.");
    return;
}


int score = quizService.getStudentScore(studentId, courseId, lesson.getLessonId());
boolean passed = quizService.isPassed(quiz, score);

if (!passed) {
    JOptionPane.showMessageDialog(this, "You must pass the quiz before marking this lesson completed.");
    return;
}

lessonService.markLessonCompleted(courseId, lesson.getLessonId(), studentId, score);
JOptionPane.showMessageDialog(this, "Lesson marked completed");
updateProgress(availableCourses.get(cidx));
    }

    private void onTakeQuizClicked() {
        int cidx = coursesList.getSelectedIndex();
        int lidx = lessonsList.getSelectedIndex();
        if (cidx < 0 || lidx < 0 || availableCourses == null) {
            JOptionPane.showMessageDialog(this, "Select course and lesson first");
            return;
        }

        Course course = availableCourses.get(cidx);
        Lesson lesson = lessonService.getLessons(course.getCourseId()).get(lidx);
System.out.println("Quiz loaded: " + lesson.getQuiz());
System.out.println("Questions: " + (lesson.getQuiz() != null ? lesson.getQuiz().getQuestionCount() : "null"));

Quiz quiz = lesson.getQuiz();
if (quiz == null || quiz.getQuestions().isEmpty()) {
    JOptionPane.showMessageDialog(this, "No quiz available for this lesson.");
    return;
}

   
        if (lidx > 0) {
            Lesson prevLesson = course.getLessons().get(lidx - 1);
            boolean prevPassed = quizService.hasPassedLesson(studentId,
                    String.valueOf(course.getCourseId()),
                    String.valueOf(prevLesson.getLessonId()));
            if (!prevPassed) {
                JOptionPane.showMessageDialog(this,
                        "You must pass the previous lesson quiz before accessing this one.",
                        "Access Denied",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
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
    }

    private void loadCourses() {
        availableCourses = studentManager.browseCourses();
        coursesListModel.clear();
        if (availableCourses != null) {
            for (Course c : availableCourses) {
                if ("APPROVED".equalsIgnoreCase(c.getStatus())) {
                coursesListModel.addElement(c.getCourseId() + " — " + c.getTitle());
                }
            }
        }
        lessonsListModel.clear();
        lessonContentArea.setText("");
        progressBar.setValue(0);
        progressLabel.setText("Progress: 0% (0/0 lessons)");
    }

        private void updateProgress(Course course) {
        List<Integer> completedLessons = studentManager.getCompletedLessons(studentId, course.getCourseId());
        int total = course.getLessons().size();
        int done = completedLessons != null ? completedLessons.size() : 0;

        int percent = total > 0 ? (done * 100 / total) : 0;
        progressBar.setValue(percent);
        progressLabel.setText("Progress: " + percent + "% (" + done + "/" + total + " lessons)");
    }
    

}