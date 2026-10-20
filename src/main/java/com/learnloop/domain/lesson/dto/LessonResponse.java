package com.learnloop.domain.lesson.dto;

import com.learnloop.domain.lesson.entity.ContentType;
import com.learnloop.domain.lesson.entity.Lesson;
import com.learnloop.domain.lesson.entity.LessonStatus;

import java.time.LocalDateTime;

public class LessonResponse {

    private String id;
    private String courseId;
    private String title;
    private String description;
    private ContentType contentType;
    private String contentUrlOrText;
    private int position;
    private LessonStatus status;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LessonResponse() {
    }

    public static LessonResponse fromEntity(Lesson lesson, boolean includeContent, boolean completed) {
        LessonResponse response = new LessonResponse();
        response.setId(lesson.getId());
        response.setCourseId(lesson.getCourse().getId());
        response.setTitle(lesson.getTitle());
        response.setDescription(lesson.getDescription());
        response.setContentType(lesson.getContentType());
        response.setContentUrlOrText(includeContent ? lesson.getContentUrlOrText() : null);
        response.setPosition(lesson.getPosition());
        response.setStatus(lesson.getStatus());
        response.setCompleted(completed);
        response.setCreatedAt(lesson.getCreatedAt());
        response.setUpdatedAt(lesson.getUpdatedAt());
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public LessonStatus getStatus() {
        return status;
    }

    public void setStatus(LessonStatus status) {
        this.status = status;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
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
