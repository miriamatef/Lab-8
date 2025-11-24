package skillforge;

import javax.swing.*;
import java.awt.*;

public class EditCourseFrame extends JFrame {
    private final Course course;
    private final InstructorDashboardFrame parent;
    private final CourseService service;
    private JTextField tfTitle;
    private JTextArea taDesc;

    public EditCourseFrame(Course course, InstructorDashboardFrame parent) {
        super("Edit Course");
        this.course = course;
        this.parent = parent;
        this.service = new CourseService(JsonDatabaseManager.getInstance());
        init();
    }

    private void init() {
        setSize(420,300); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tfTitle = new JTextField(course.getTitle(), 30);
        taDesc = new JTextArea(course.getDescription(),6,30);
        JButton btn = new JButton("Save");

        btn.addActionListener(e -> {
            if (tfTitle.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Title required"); return; }
            boolean ok = service.editCourse(course.getCourseId(), tfTitle.getText().trim(), taDesc.getText().trim());
            if (ok) { JOptionPane.showMessageDialog(this, "Updated"); parent.loadCourses(); dispose(); }
            else JOptionPane.showMessageDialog(this, "Update failed");
        });

        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(); top.add(new JLabel("Title:")); top.add(tfTitle);
        p.add(top, BorderLayout.NORTH); p.add(new JScrollPane(taDesc), BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        add(p);
        setVisible(true);
    }
}
