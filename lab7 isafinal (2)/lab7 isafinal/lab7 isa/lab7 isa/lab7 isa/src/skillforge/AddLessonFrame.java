package skillforge;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AddLessonFrame extends JFrame {
    private final Course course;
    private final LessonService svc;
    private final LessonManagementFrame parent;
    private JTextField tfTitle;
    private JTextArea taContent;
    private JTextField tfResources;

    public AddLessonFrame(Course course, LessonService svc, LessonManagementFrame parent) {
        super("Add Lesson - " + course.getTitle());
        this.course = course; this.svc = svc; this.parent = parent;
        init();
    }

    private void init() {
        setSize(500,500); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tfTitle = new JTextField(30);
        taContent = new JTextArea(6,30);
        tfResources = new JTextField(30);
        JButton btn = new JButton("Add");

        btn.addActionListener(e -> {
            String t = tfTitle.getText().trim();
            if (t.isEmpty()) { JOptionPane.showMessageDialog(this,"Title required"); return; }
            String content = taContent.getText().trim();
            List<String> res = new ArrayList<>();
            if (!tfResources.getText().trim().isEmpty()) {
                String[] parts = tfResources.getText().split(",");
                for (String s : parts) res.add(s.trim());
            }
            Lesson l = svc.addLesson(course.getCourseId(), t, content, res);
            if (l != null) {
                JOptionPane.showMessageDialog(this,"Added");
                parent.refreshList(); dispose();
            } else JOptionPane.showMessageDialog(this,"Add failed (maybe duplicate title)");
        });

        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(); top.add(new JLabel("Title:")); top.add(tfTitle);
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(taContent), BorderLayout.CENTER);
        JPanel bot = new JPanel(); bot.add(new JLabel("Resources (comma sep):")); bot.add(tfResources); bot.add(btn);
        p.add(bot, BorderLayout.SOUTH);
        add(p);
        setVisible(true);
    }
}
