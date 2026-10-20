package com.learnloop.domain.assignment.dto;

import com.learnloop.domain.assignment.entity.Assignment;
import com.learnloop.domain.assignment.entity.AssignmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AssignmentResponse {

    private String id;
    private String courseId;
    private String courseTitle;
    private String teacherId;
    private String teacherName;
    private String title;
    private String instructions;
    private BigDecimal maxMarks;
    private LocalDateTime dueDate;
    private AssignmentStatus status;
    private boolean submitted;
    private SubmissionResponse mySubmission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AssignmentResponse() {
    }

    public static AssignmentResponse fromEntity(Assignment a, SubmissionResponse mySubmission) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(a.getId());
        response.setCourseId(a.getCourse().getId());
        response.setCourseTitle(a.getCourse().getTitle());
        response.setTeacherId(a.getTeacher().getId());
        response.setTeacherName(a.getTeacher().getFullName());
        response.setTitle(a.getTitle());
        response.setInstructions(a.getInstructions());
        response.setMaxMarks(a.getMaxMarks());
        response.setDueDate(a.getDueDate());
        response.setStatus(a.getStatus());
        response.setSubmitted(mySubmission != null);
        response.setMySubmission(mySubmission);
        response.setCreatedAt(a.getCreatedAt());
        response.setUpdatedAt(a.getUpdatedAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public BigDecimal getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(BigDecimal maxMarks) {
        this.maxMarks = maxMarks;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public SubmissionResponse getMySubmission() {
        return mySubmission;
    }

    public void setMySubmission(SubmissionResponse mySubmission) {
        this.mySubmission = mySubmission;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
