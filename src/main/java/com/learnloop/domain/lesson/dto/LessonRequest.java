package com.learnloop.domain.lesson.dto;

import com.learnloop.domain.lesson.entity.ContentType;
import com.learnloop.domain.lesson.entity.LessonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LessonRequest {

    @NotBlank(message = "Lesson title is required")
    @Size(min = 2, max = 200, message = "Lesson title must be between 2 and 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Content type is required")
    private ContentType contentType = ContentType.TEXT;

    @NotBlank(message = "Content or URL is required")
    private String contentUrlOrText;

    private Integer position;

    private LessonStatus status = LessonStatus.PUBLISHED;

    public LessonRequest() {
    }

    public LessonRequest(String title, String description, ContentType contentType,
                         String contentUrlOrText, Integer position, LessonStatus status) {
        this.title = title;
        this.description = description;
        this.contentType = contentType;
        this.contentUrlOrText = contentUrlOrText;
        this.position = position;
        this.status = status != null ? status : LessonStatus.PUBLISHED;
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

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getContentUrlOrText() {
        return contentUrlOrText;
    }

    public void setContentUrlOrText(String contentUrlOrText) {
        this.contentUrlOrText = contentUrlOrText;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public LessonStatus getStatus() {
        return status;
    }

    public void setStatus(LessonStatus status) {
        this.status = status;
    }
}
