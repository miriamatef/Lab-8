package skillforge;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditLessonFrame extends JFrame {
    private final Course course;
    private final Lesson lesson;
    private final LessonService svc;
    private final LessonManagementFrame parent;
    private JTextField tfTitle;
    private JTextArea taContent;
    private JTextField tfResources;

    public EditLessonFrame(Course course, Lesson lesson, LessonService svc, LessonManagementFrame parent) {
        super("Edit Lesson");
        this.course = course; this.lesson = lesson; this.svc = svc; this.parent = parent;
        init();
    }

    private void init() {
        setSize(420,420); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        tfTitle = new JTextField(lesson.getTitle(), 30);
        taContent = new JTextArea(lesson.getContent(),6,30);
        tfResources = new JTextField(String.join(", ", lesson.getResources()), 30);
        JButton btn = new JButton("Save");

        btn.addActionListener(e -> {
            String t = tfTitle.getText().trim();
            if (t.isEmpty()) { JOptionPane.showMessageDialog(this,"Title required"); return; }
            String content = taContent.getText().trim();
            List<String> res = new ArrayList<>();
            if (!tfResources.getText().trim().isEmpty()) {
                for (String s : tfResources.getText().split(",")) res.add(s.trim());
            }
            boolean ok = svc.editLesson(course.getCourseId(), lesson.getLessonId(), t, content, res);
            if (ok) {
                JOptionPane.showMessageDialog(this,"Saved");
                parent.refreshList(); dispose();
            } else JOptionPane.showMessageDialog(this,"Save failed");
        });

        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(); top.add(new JLabel("Title:")); top.add(tfTitle);
        p.add(top, BorderLayout.NORTH); p.add(new JScrollPane(taContent), BorderLayout.CENTER);
        JPanel bot = new JPanel(); bot.add(new JLabel("Resources:")); bot.add(tfResources); bot.add(btn);
        p.add(bot, BorderLayout.SOUTH);
        add(p);
        setVisible(true);
    }
}
