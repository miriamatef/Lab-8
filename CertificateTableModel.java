/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;

/**
 *
 * @author karen
 */

import java.time.LocalDate;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

class CertificateTableModel extends AbstractTableModel {
    private List<Certificate> data = new ArrayList<>();
    private final String[] cols = {"Certificate ID", "Course", "Issue Date"};

    public void setCertificates(JSONArray certs) {
    data = new ArrayList<>();  // reset the list

    if (certs != null) {
        for (Object o : certs) {
            JSONObject obj = (JSONObject) o;

            Certificate c = new Certificate(
                obj.optString("certificateId"),
                LocalDate.parse(obj.optString("issueDate")),   // convert to LocalDate
                obj.optString("studentId"),
                obj.optString("courseId"),
                obj.optString("courseTitle")
            );

            data.add(c);
        }
    }

    fireTableDataChanged();
}



    public Certificate getCertificateAt(int row) {
        if (row < 0 || row >= data.size()) return null;
        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int column) {
        return cols[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Certificate c = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return c.getCertificateId();
            case 1: return c.getCourseTitle();
            case 2: return c.getIssueDate().toString();
            default: return "";
        }
    }
}
