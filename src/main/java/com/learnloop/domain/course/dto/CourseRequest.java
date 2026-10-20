package com.learnloop.domain.course.dto;

import com.learnloop.domain.course.entity.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CourseRequest {

    @NotBlank(message = "Course title is required")
    @Size(min = 3, max = 200, message = "Course title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Short description is required")
    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    private String shortDescription;

    @NotBlank(message = "Detailed description is required")
    private String detailedDescription;

    @NotBlank(message = "Subject is required")
    @Size(max = 100, message = "Subject cannot exceed 100 characters")
    private String subject;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel = DifficultyLevel.BEGINNER;

    @Size(max = 500, message = "Thumbnail URL cannot exceed 500 characters")
    private String thumbnailUrl;

    public CourseRequest() {
    }

    public CourseRequest(String title, String shortDescription, String detailedDescription,
                         String subject, DifficultyLevel difficultyLevel, String thumbnailUrl) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.detailedDescription = detailedDescription;
        this.subject = subject;
        this.difficultyLevel = difficultyLevel;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
