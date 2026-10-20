package com.learnloop.domain.kafka.event;

import java.math.BigDecimal;

public class AssignmentGradedEvent extends BaseEvent {

    private String assignmentId;
    private String assignmentTitle;
    private String studentId;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;
    private String gradedById;
    private String courseId;

    public AssignmentGradedEvent() {
        super();
    }

    public AssignmentGradedEvent(String assignmentId, String assignmentTitle, String studentId,
                                 BigDecimal marksObtained, BigDecimal maxMarks, String gradedById, String courseId) {
        super("ASSIGNMENT_GRADED", assignmentId);
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.studentId = studentId;
        this.marksObtained = marksObtained;
        this.maxMarks = maxMarks;
        this.gradedById = gradedById;
        this.courseId = courseId;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(BigDecimal marksObtained) {
        this.marksObtained = marksObtained;
    }

    public BigDecimal getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(BigDecimal maxMarks) {
        this.maxMarks = maxMarks;
    }

    public String getGradedById() {
        return gradedById;
    }

    public void setGradedById(String gradedById) {
        this.gradedById = gradedById;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}
