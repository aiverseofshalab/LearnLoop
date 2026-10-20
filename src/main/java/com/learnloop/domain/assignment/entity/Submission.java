package com.learnloop.domain.assignment.entity;

import com.learnloop.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_submissions_assignment_student", columnNames = {"assignment_id", "student_id"})
})
public class Submission {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "text_content", columnDefinition = "TEXT", nullable = false)
    private String textContent;

    @Column(name = "resource_url", length = 500)
    private String resourceUrl;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "marks_obtained", precision = 5, scale = 2)
    private BigDecimal marksObtained;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by")
    private User gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Submission() {
    }

    public Submission(Assignment assignment, User student, String textContent, String resourceUrl) {
        this.id = UUID.randomUUID().toString();
        this.assignment = assignment;
        this.student = student;
        this.textContent = textContent;
        this.resourceUrl = resourceUrl;
        this.status = SubmissionStatus.SUBMITTED;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
        this.updatedAt = this.submittedAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
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

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public User getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(User gradedBy) {
        this.gradedBy = gradedBy;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
