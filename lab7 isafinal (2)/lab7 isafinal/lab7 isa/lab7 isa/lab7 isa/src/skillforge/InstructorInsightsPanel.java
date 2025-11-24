/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package skillforge;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Miriam
 */


public class InstructorInsightsPanel extends JPanel {
    private final AnalyticsService analytics;
    private final int courseId;
    private JTable lessonTable; //a table to display lesson stats.
    private JLabel labelEnrolled; //label for enrolled count
    private JLabel labelOverallAvg; //label for overall average
    private JButton buttonRefresh;
    private JButton buttonCharts;

    public InstructorInsightsPanel(AnalyticsService analytics, int courseId) {
        this.analytics = analytics;
        this.courseId = courseId;
        initUI();
        loadDataAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8)); //vertical and horizontal padding of 8
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelEnrolled = new JLabel("Enrolled: -");
        labelOverallAvg = new JLabel("Overall Avg: -");
        top.add(labelEnrolled);
        top.add(Box.createHorizontalStrut(10)); //Adds a horizontal spacer (10px).
        top.add(labelOverallAvg);

        add(top, BorderLayout.NORTH);

        lessonTable = new JTable();
        add(new JScrollPane(lessonTable), BorderLayout.CENTER);
        //Places it in a scroll pane so it can scroll large data

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonRefresh = new JButton("Refresh");
        buttonCharts = new JButton("View Charts");
        bottom.add(buttonRefresh);
        bottom.add(buttonCharts);
        add(bottom, BorderLayout.SOUTH);

        buttonRefresh.addActionListener(e -> loadDataAsync());
        buttonCharts.addActionListener(e -> openCharts());
    }

    private void loadDataAsync() {
        buttonRefresh.setEnabled(false); //Disables refresh button so user can’t spam it
        
        //Creating a SwingWorker:
        //1-Runs heavy code in background.
        //2-Updates UI on EDT (Event Dispatch Thread).
        SwingWorker<CourseStats, Void> worker = new SwingWorker<>() {
            
            @Override
            protected CourseStats doInBackground() throws Exception {
                return analytics.calculateCourseStats(courseId);
            }

            @Override
            protected void done() { //Runs on UI thread after background finishes.
                try {
                    CourseStats stats = get(); //get() retrieves the result from doInBackground()
                    populateFromStats(stats); //Fills UI labels & table using populateFromStats.
                } 
                catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(InstructorInsightsPanel.this, "Failed to load analytics: " + ex.getCause(), "Error", JOptionPane.ERROR_MESSAGE);
                } 
                finally {
                    buttonRefresh.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void populateFromStats(CourseStats stats) {
        labelEnrolled.setText("Enrolled: " + stats.getEnrolledCount());
        labelOverallAvg.setText("Overall Avg: " + stats.getAverage() + "%");

        String[] cols = {"Lesson ID","Lesson Title","Avg Score (%)","Completion (%)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { 
                return false; //makes cells non-editable.
            }
        };
        
        for (LessonStats ls : stats.getLessons()) {
            model.addRow(new Object[]{ls.getLessonId(), ls.getLessonTitle(), ls.getAverageScore(), ls.getCompletionPercent()});
        }
        lessonTable.setModel(model); 
    }

    private void openCharts() {
        buttonCharts.setEnabled(false); //Prevents double clicks.
        SwingWorker<CourseStats, Void> worker = new SwingWorker<>() {
            @Override
            protected CourseStats doInBackground() throws Exception {
                return analytics.calculateCourseStats(courseId);
            }
            @Override
            protected void done() {
                try {
                    CourseStats stats = get();
                    ChartFrame frame = new ChartFrame(stats);
                    frame.setLocationRelativeTo(InstructorInsightsPanel.this);
                    frame.setVisible(true);
                } 
                catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(InstructorInsightsPanel.this, "Failed to open charts: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } 
                finally {
                    buttonCharts.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
