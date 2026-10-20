package com.learnloop.domain.assignment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class GradeSubmissionRequest {

    @NotNull(message = "Marks obtained is required")
    @DecimalMin(value = "0.0", message = "Marks cannot be negative")
    private BigDecimal marksObtained;

    @Size(max = 2000, message = "Feedback cannot exceed 2000 characters")
    private String feedback;

    public GradeSubmissionRequest() {
    }

    public GradeSubmissionRequest(BigDecimal marksObtained, String feedback) {
        this.marksObtained = marksObtained;
        this.feedback = feedback;
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
}
