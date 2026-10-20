package com.learnloop.domain.assignment.dto;

import com.learnloop.domain.assignment.entity.Submission;
import com.learnloop.domain.assignment.entity.SubmissionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SubmissionResponse {

    private String id;
    private String assignmentId;
    private String assignmentTitle;
    private String studentId;
    private String studentName;
    private String textContent;
    private String resourceUrl;
    private SubmissionStatus status;
    private BigDecimal marksObtained;
    private BigDecimal maxMarks;
    private String feedback;
    private String gradedByName;
    private LocalDateTime gradedAt;
    private LocalDateTime submittedAt;

    public SubmissionResponse() {
    }

    public static SubmissionResponse fromEntity(Submission s) {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(s.getId());
        response.setAssignmentId(s.getAssignment().getId());
        response.setAssignmentTitle(s.getAssignment().getTitle());
        response.setStudentId(s.getStudent().getId());
        response.setStudentName(s.getStudent().getFullName());
        response.setTextContent(s.getTextContent());
        response.setResourceUrl(s.getResourceUrl());
        response.setStatus(s.getStatus());
        response.setMarksObtained(s.getMarksObtained());
        response.setMaxMarks(s.getAssignment().getMaxMarks());
        response.setFeedback(s.getFeedback());
        if (s.getGradedBy() != null) {
            response.setGradedByName(s.getGradedBy().getFullName());
        }
        response.setGradedAt(s.getGradedAt());
        response.setSubmittedAt(s.getSubmittedAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
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

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getGradedByName() {
        return gradedByName;
    }

    public void setGradedByName(String gradedByName) {
        this.gradedByName = gradedByName;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
