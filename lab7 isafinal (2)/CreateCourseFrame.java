package skillforge;

import javax.swing.*;
import java.awt.*;

public class CreateCourseFrame extends JFrame {
    private final String instructorId;
    private final InstructorDashboardFrame parent;
    private final CourseService courseService;

    private JTextField tfTitle;
    private JTextArea taDesc;

    public CreateCourseFrame(String instructorId, InstructorDashboardFrame parent) {
        super("Create Course");
        this.instructorId = instructorId;
        this.parent = parent;
        this.courseService = new CourseService(JsonDatabaseManager.getInstance());
        init();
    }

    private void init() {
        setSize(400,300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tfTitle = new JTextField(30);
        taDesc = new JTextArea(6,30);
        JButton btn = new JButton("Create");

        btn.addActionListener(e -> {
            String t = tfTitle.getText().trim(); String d = taDesc.getText().trim();
            Course c = courseService.createCourse(instructorId, t, d);
            if (c != null) { JOptionPane.showMessageDialog(this, "Created id: "+c.getCourseId()); parent.loadCourses(); dispose(); }
            else JOptionPane.showMessageDialog(this, "Create failed. Possibly duplicate title.");
        });

        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(); top.add(new JLabel("Title:")); top.add(tfTitle);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(taDesc), BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        add(p);
        setVisible(true);
    }
}
