/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;

/**
 *
 * @author karen
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;

public class CertificateViewFrame extends JFrame {

    private final Certificate certificate;

    public CertificateViewFrame(Certificate certificate) {
        this.certificate = certificate;
        setTitle("Certificate: " + certificate.getCourseTitle());
        setSize(480, 320);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setText(formatCertificateText());
        p.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton saveBtn = new JButton("Save as JSON");
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int ret = fc.showSaveDialog(CertificateViewFrame.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                        fw.write(formatCertificateJson().toString(4));
                        JOptionPane.showMessageDialog(CertificateViewFrame.this, "Saved successfully.");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(CertificateViewFrame.this, "Failed to save: " + ex.getMessage());
                    }
                }
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(saveBtn);
        p.add(bottom, BorderLayout.SOUTH);

        setContentPane(p);
    }

    private JSONObject formatCertificateJson() {
        JSONObject obj = new JSONObject();
        obj.put("certificateId", certificate.getCertificateId());
        obj.put("issueDate", certificate.getIssueDate().toString());
        obj.put("studentId", certificate.getStudentId());
        obj.put("courseId", certificate.getCourseId());
        obj.put("courseTitle", certificate.getCourseTitle());
        return obj;
    }

    private String formatCertificateText() {
        return formatCertificateJson().toString(4);
    }
}
