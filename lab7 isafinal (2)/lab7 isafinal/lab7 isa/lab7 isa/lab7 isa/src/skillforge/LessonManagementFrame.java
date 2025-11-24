package skillforge;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LessonManagementFrame extends JFrame {
    private final Course course;
    private final LessonService lessonService;
    private final DefaultListModel<Lesson> lm;
    private final JList<Lesson> lessonJList;

    public LessonManagementFrame(Course course) {
    super("Manage Lessons - " + course.getTitle());
    this.course = course;
    this.lessonService = new LessonService(JsonDatabaseManager.getInstance());
    setSize(800, 650);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    lm = new DefaultListModel<>();
    lessonJList = new JList<>(lm);
    loadLessons();

    JButton add = new JButton("Add");
    JButton edit = new JButton("Edit");
    JButton delete = new JButton("Delete");

    add.addActionListener(e -> {
        AddLessonFrame addFrame = new AddLessonFrame(course, lessonService, this);
        addFrame.setSize(700, 600); 
        addFrame.setLocationRelativeTo(this);
        addFrame.setVisible(true);
    });

    edit.addActionListener(e -> {
        Lesson sel = lessonJList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select");
            return;
        }
        new EditLessonFrame(course, sel, lessonService, this);
    });

    delete.addActionListener(e -> {
        Lesson sel = lessonJList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete " + sel.getTitle() + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            boolean ok = lessonService.deleteLesson(course.getCourseId(), sel.getLessonId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Deleted");
                refreshList();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed");
            }
        }
    });

    JPanel bottom = new JPanel();
    bottom.add(add);
    bottom.add(edit);
    bottom.add(delete);

    add(new JScrollPane(lessonJList), BorderLayout.CENTER);
    add(bottom, BorderLayout.SOUTH);
    setVisible(true);
}


    void loadLessons() {
        lm.clear();
        for (Lesson l : course.getLessons()) lm.addElement(l);
    }

    public void refreshList() {
        CourseService cs = new CourseService(JsonDatabaseManager.getInstance());
        Course fresh = null;
        for (Course c : cs.getAllCourses()) if (c.getCourseId() == course.getCourseId()) { fresh = c; break; }
        if (fresh != null) {
            course.setLessons(fresh.getLessons());
            loadLessons();
        }
    }
}
