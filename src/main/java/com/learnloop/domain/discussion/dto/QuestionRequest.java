package com.learnloop.domain.discussion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QuestionRequest {

    @NotBlank(message = "Course ID is required")
    private String courseId;

    private String lessonId;

    @NotBlank(message = "Question title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    public QuestionRequest() {
    }

    public QuestionRequest(String courseId, String lessonId, String title, String description) {
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.title = title;
        this.description = description;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
