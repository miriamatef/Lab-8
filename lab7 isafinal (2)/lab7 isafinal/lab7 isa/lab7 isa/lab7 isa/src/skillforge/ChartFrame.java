/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 *
 * @author Miriam
 */

public class ChartFrame extends JFrame{
    private final CourseStats stats;
    
    public ChartFrame(CourseStats stats) {
        super("Analytics — " + stats.getTitle()); 
        //Calls JFrame constructor to set the window title, the title becomes Analytics -(course title)
        
        this.stats = stats;
        setSize(900,460); //width= 900px, height = 460px
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //to close only this window not the whole program
        
        init();
    }
    
    private void init() {
        setLayout(new GridLayout(1,2));
        add(new ChartPanel(stats, ChartPanel.ChartType.BAR_AVG)); //Displays bar chart for average score.
        add(new ChartPanel(stats, ChartPanel.ChartType.LINE_COMPLETION)); //Displays line chart for completion %.
    }
    
    
    //inner class for drawing the charts
    static class ChartPanel extends JPanel {
        enum ChartType { BAR_AVG, LINE_COMPLETION }
        /*Two chart types:
        BAR_AVG → bar chart of average score
        LINE_COMPLETION → line graph of completion %*/
        
        private final CourseStats stats;//data to draw
        private final ChartType type;//which chart to draw

        ChartPanel(CourseStats stats, ChartType type) {
            this.stats = stats;
            this.type = type;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            //Clears background before drawing, a mandatory step when drawing custom graphics
            
            if (stats.getLessons() == null || stats.getLessons().isEmpty()) {
                g.drawString("No data to display", 20, 20);
                return;
            }
            
            Graphics2D g2 = (Graphics2D) g;
            //Graphics2D allows: better shapes, better lines, anti-aliasing, more control over drawing

            int w = getWidth(), h = getHeight();
            int margin = 60; //Sets 60px margin around chart.
            int chartW = w - 2*margin;
            int chartH = h - 2*margin;
            //chartW and chartH are drawable areas
            
            g2.drawLine(margin, margin, margin, margin+chartH); //Y axis (top->buttom)
            g2.drawLine(margin, margin+chartH, margin+chartW, margin+chartH); //X axis (left -> right)

            int n = stats.getLessons().size(); //number of lessons
            int slotWidth = chartW / Math.max(1, n);

            double max = 100.0;//Max possible value for scaling (100%).

            if (type == ChartType.BAR_AVG) {
                for (int i=0;i<n;i++) {
                    LessonStats ls = stats.getLessons().get(i);
                    double val = ls.getAverageScore();
                    int barH = (int)((val / max) * chartH); //Convert 0–100 score to a height proportional to chart height.
                    int x = margin + i*slotWidth + 10; //x= left margin + slot offset
                    int y = margin + chartH - barH; //y= chart bottom minus bar height
                    int width = Math.max(10, slotWidth - 20); //size of bar;
                    g2.fillRect(x, y, width, barH); //Draws the actual bar.
                    g2.drawString(truncate(ls.getLessonTitle(), 12), x, margin + chartH + 15);
                    g2.drawString(String.valueOf(ls.getAverageScore()), x, y - 6);
                    //lesson title under the bar and score above the bar
                }
                g2.drawString("Average Score (%) per Lesson", margin, margin - 20);//chart title
            } 
            else { //line chart
                int[] xs = new int[n];
                int[] ys = new int[n];
                for (int i=0;i<n;i++) {
                    LessonStats ls = stats.getLessons().get(i);
                    xs[i] = margin + i * (chartW / Math.max(1, n-1)); //Spread X values evenly.
                    ys[i] = margin + chartH - (int)((ls.getCompletionPercent() / max) * chartH); //Scale Y values from completion percentage.
                }
                g2.setStroke(new BasicStroke(2f));
                for (int i=0;i<n-1;i++) 
                    g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]); //lines between points
                for (int i=0;i<n;i++) {
                    g2.fillOval(xs[i]-4, ys[i]-4, 8,8); //marks each point
                    g2.drawString(truncate(stats.getLessons().get(i).getLessonTitle(), 12), xs[i]-15, margin+chartH+15);
                    //title below point
                    g2.drawString(String.valueOf(stats.getLessons().get(i).getCompletionPercent()), xs[i]-10, ys[i]-10);
                    //value above the point
                }
                g2.drawString("Completion % per Lesson", margin, margin - 20); //line chart title
            }
        }

        private String truncate(String s, int len) {
            //Prevents long lesson titles from overflowing, it cuts text and adds "..." if too long.
            if (s == null) 
                return "";
            return s.length() <= len ? s : s.substring(0, len-1) + "…";
        }
    }
}
