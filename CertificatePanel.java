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
import java.util.List;


public class CertificatePanel extends JPanel {
    private final String studentId;
    private final JsonDatabaseManager dbManager;

    private JTable table;
    private CertificateTableModel tableModel;

    public CertificatePanel(String studentId, JsonDatabaseManager dbManager) {
        this.studentId = studentId;
        this.dbManager = dbManager;
        initUI();
        loadCertificates();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        tableModel = new CertificateTableModel();
        table = new JTable(tableModel);
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        JButton viewBtn = new JButton("View Certificate");
        viewBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sel = table.getSelectedRow();
                if (sel < 0) {
                    JOptionPane.showMessageDialog(CertificatePanel.this, "Please select a certificate to view.");
                    return;
                }
                Certificate cert = tableModel.getCertificateAt(sel);
                CertificateViewFrame frame = new CertificateViewFrame(cert);
                frame.setLocationRelativeTo(CertificatePanel.this);
                frame.setVisible(true);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(viewBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadCertificates() {
        User u = dbManager.findUserById(studentId).orElse(null);
        if (u == null) return;

        List<Certificate> certs = u.getCertificates();
        tableModel.setCertificates(certs);
    }
}
