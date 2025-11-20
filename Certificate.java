package skillforge;

import java.time.LocalDate;
import java.util.Objects;


public class Certificate {
private String certificateId;
private LocalDate issueDate;
private String studentId;
private String courseId;
private String courseTitle;


public Certificate() { }


public Certificate(String certificateId, LocalDate issueDate, String studentId, String courseId, String courseTitle) {
this.certificateId = certificateId;
this.issueDate = issueDate;
this.studentId = studentId;
this.courseId = courseId;
this.courseTitle = courseTitle;
}


public String getCertificateId() {
return certificateId;
}


public void setCertificateId(String certificateId) {
this.certificateId = certificateId;
}


public LocalDate getIssueDate() {
return issueDate;
}


public void setIssueDate(LocalDate issueDate) {
this.issueDate = issueDate;
}


public String getStudentId() {
return studentId;
}


public void setStudentId(String studentId) {
this.studentId = studentId;
}


public String getCourseId() {
return courseId;
}


public void setCourseId(String courseId) {
this.courseId = courseId;
}


public String getCourseTitle() {
return courseTitle;
}


public void setCourseTitle(String courseTitle) {
this.courseTitle = courseTitle;
}


@Override
public boolean equals(Object o) {
if (this == o) return true;
if (!(o instanceof Certificate)) return false;
Certificate that = (Certificate) o;
return Objects.equals(certificateId, that.certificateId);
}


@Override
public int hashCode() {
return Objects.hash(certificateId);
}


@Override
public String toString() {
return "Certificate{" +
"certificateId='" + certificateId + '\'' +
", issueDate=" + issueDate +
", studentId='" + studentId + '\'' +
", courseId='" + courseId + '\'' +
", courseTitle='" + courseTitle + '\'' +
'}';
}
}