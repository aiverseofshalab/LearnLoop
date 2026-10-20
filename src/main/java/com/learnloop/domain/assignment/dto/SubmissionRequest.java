package com.learnloop.domain.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubmissionRequest {

    @NotBlank(message = "Submission text content is required")
    private String textContent;

    @Size(max = 500, message = "Resource URL cannot exceed 500 characters")
    private String resourceUrl;

    public SubmissionRequest() {
    }

    public SubmissionRequest(String textContent, String resourceUrl) {
        this.textContent = textContent;
        this.resourceUrl = resourceUrl;
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
}
