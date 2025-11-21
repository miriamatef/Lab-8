package skillforge;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InstructorDashboardFrame extends JFrame {

    private final String instructorId;
    private final CourseService courseService;
    private final UserService userService;
    private final LoginFrame loginFrame; //allows returning to login upon logout

    private final DefaultListModel<Course> listModel; //stores all courses owned by instructor
    private final JList<Course> courseJList;

    private DefaultListModel<String> studentListModel;
    private JList<String> studentJList; //shows students enrolled in the selected course

    public InstructorDashboardFrame(String instructorId, UserService userService, LoginFrame loginFrame) {

        this.instructorId = instructorId;
        this.userService = userService;
        this.loginFrame = loginFrame;
        this.courseService = new CourseService(userService.db);

        setTitle("Instructor Dashboard");
        setSize(980, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        listModel = new DefaultListModel<>();
        courseJList = new JList<>(listModel);
        courseJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        initUI();
        loadCourses();
        setVisible(true);
    }

    private void initUI() {
        // TOP BAR (LOGOUT)
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            loginFrame.setVisible(true); //reopen login frame
            dispose(); // close the dashboard frame
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(logoutBtn);
        add(topBar, BorderLayout.NORTH);


        // LEFT MAIN COURSE LIST
        add(new JScrollPane(courseJList), BorderLayout.CENTER); 
        //shows instructor's courses (scrollable)


        //BOTTOM ACTION BUTTONS
        JButton create = new JButton("Create");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Delete");
        JButton lessons = new JButton("Manage Lessons");
        JButton analytics = new JButton("Analytics / Insights");  // NEW
        JButton quizzes = new JButton("Manage Quizzes");          // NEW

        //create button
        create.addActionListener(e -> new CreateCourseFrame(instructorId, this));

        //edit button
        edit.addActionListener(e -> {
            Course c = courseJList.getSelectedValue();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Select a course.");
                return;
            }
            new EditCourseFrame(c, this);
        });

        //delete button
        del.addActionListener(e -> {
            Course c = courseJList.getSelectedValue();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Select a course.");
                return;
            }
            
            if (JOptionPane.showConfirmDialog(this,"Delete " + c.getTitle() + "?","Confirm",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                if (courseService.deleteCourse(c.getCourseId())) {
                    JOptionPane.showMessageDialog(this, "Course deleted.");
                    loadCourses();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed.");
                }
            }
        });

        //lessons button
        lessons.addActionListener(e -> {
            Course c = courseJList.getSelectedValue();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Select a course.");
                return;
            }
            new LessonManagementFrame(c);
        });

        // Instructor Insights Panel
        analytics.addActionListener(e -> {
            Course c = courseJList.getSelectedValue();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Select a course first.");
                return;
            }
            AnalyticsService analyticsService = new AnalyticsService();
            JFrame insightsFrame = new JFrame("Insights — " + c.getTitle());
            insightsFrame.setSize(860, 500);
            insightsFrame.add(new InstructorInsightsPanel(analyticsService, c.getCourseId()));
            insightsFrame.setLocationRelativeTo(this);
            insightsFrame.setVisible(true);
        });

        // Quiz Management
        quizzes.addActionListener(e -> {
            Course c = courseJList.getSelectedValue();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Select a course first.");
                return;
            }
            new QuizManagementFrame(c);   
        });

         
        JPanel bottom = new JPanel();
        bottom.add(create);
        bottom.add(edit);
        bottom.add(del);
        bottom.add(lessons);
        bottom.add(analytics);
        bottom.add(quizzes);

        add(bottom, BorderLayout.SOUTH);


        // RIGHT PANEL: STUDENT LIST
        studentListModel = new DefaultListModel<>();
        studentJList = new JList<>(studentListModel);

        JScrollPane studentScroll = new JScrollPane(studentJList);
        studentScroll.setPreferredSize(new Dimension(250, 0));

        JPanel studentPanel = new JPanel(new BorderLayout());
        studentPanel.add(new JLabel("Enrolled Students", JLabel.CENTER), BorderLayout.NORTH);
        studentPanel.add(studentScroll, BorderLayout.CENTER);
        //this shows a sidebar with enrolled students of the selected course

        add(studentPanel, BorderLayout.EAST);


        //UPDATE STUDENT LIST WHEN COURSE SELECTED CHANGES
        courseJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Course c = courseJList.getSelectedValue();
                if (c != null) updateStudentList(c);
            }
        });
    }

    private void updateStudentList(Course course) {
        studentListModel.clear(); //clears previously displayed students
        List<String> students = course.getStudents();

        if (students != null) {
            for (String s : students) studentListModel.addElement(s);
        }
    }

    public void loadCourses() {
        listModel.clear(); //remove old data
        List<Course> courses = courseService.getCoursesByInstructor(instructorId);
        for (Course c : courses) 
            listModel.addElement(c); //adds each course to the JList
    }
}
