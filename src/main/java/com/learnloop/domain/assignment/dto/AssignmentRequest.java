package com.learnloop.domain.assignment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AssignmentRequest {

    @NotBlank(message = "Assignment title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @NotBlank(message = "Instructions are required")
    private String instructions;

    @NotNull(message = "Maximum marks is required")
    @DecimalMin(value = "1.0", message = "Max marks must be at least 1.0")
    private BigDecimal maxMarks = new BigDecimal("100.00");

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    public AssignmentRequest() {
    }

    public AssignmentRequest(String title, String instructions, BigDecimal maxMarks, LocalDateTime dueDate) {
        this.title = title;
        this.instructions = instructions;
        this.maxMarks = maxMarks;
        this.dueDate = dueDate;
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
}
