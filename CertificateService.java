
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package skillforge;

/**
 *
 * @author karen
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

public class CertificateService {

    private final JsonDatabaseManager db = JsonDatabaseManager.getInstance();

    public CertificateService() {}

    public Certificate generateCertificate(String studentId, String courseId) throws IOException {

        JSONArray users = db.readUsers();
        JSONArray courses = db.readCourses();

        JSONObject student = findById(users, studentId);
        JSONObject course = findById(courses, courseId);

        if (student == null) throw new IllegalArgumentException("Student not found");
        if (course == null) throw new IllegalArgumentException("Course not found");

        String certId = UUID.randomUUID().toString();
        LocalDate issueDate = LocalDate.now();

        Certificate cert = new Certificate(certId, issueDate, studentId, courseId, course.optString("title"));

       
        JSONObject certJson = new JSONObject();
        certJson.put("certificateId", certId);
        certJson.put("issueDate", issueDate.toString());
        certJson.put("studentId", studentId);
        certJson.put("courseId", courseId);
        certJson.put("courseTitle", course.optString("title"));

     
        JSONArray certs = student.optJSONArray("certificates");
        if (certs == null) {
            certs = new JSONArray();
            student.put("certificates", certs);
        }
        certs.put(certJson);

        
        db.writeUsers(users);

       
        saveCertificateAsJson(certJson);

        return cert;
    }

    private JSONObject findById(JSONArray array, String id) {
        for (Object o : array) {
            JSONObject obj = (JSONObject) o;
            if (obj.optString("id").equals(id)) return obj;
        }
        return null;
    }

    private void saveCertificateAsJson(JSONObject certJson) throws IOException {
        String filename = "certificates/" + certJson.getString("certificateId") + ".json";
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(certJson.toString(4));
        }
    }
}
