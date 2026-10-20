package com.learnloop.domain.kafka.event;

public class EnrollmentEvent extends BaseEvent {

    private String studentId;
    private String studentName;
    private String studentEmail;
    private String courseId;
    private String courseTitle;

    public EnrollmentEvent() {
        super();
    }

    public EnrollmentEvent(String studentId, String studentName, String studentEmail,
                           String courseId, String courseTitle) {
        super("ENROLLMENT_CONFIRMED", courseId);
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
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
}
