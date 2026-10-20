package com.learnloop.domain.course.dto;

import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.entity.CourseStatus;
import com.learnloop.domain.course.entity.DifficultyLevel;

import java.time.LocalDateTime;

public class CourseResponse {

    private String id;
    private String title;
    private String shortDescription;
    private String subject;
    private DifficultyLevel difficultyLevel;
    private String teacherId;
    private String teacherName;
    private String thumbnailUrl;
    private CourseStatus status;
    private long lessonCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CourseResponse() {
    }

    public static CourseResponse fromEntity(Course course, long lessonCount) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setShortDescription(course.getShortDescription());
        response.setSubject(course.getSubject());
        response.setDifficultyLevel(course.getDifficultyLevel());
        response.setTeacherId(course.getTeacher().getId());
        response.setTeacherName(course.getTeacher().getFullName());
        response.setThumbnailUrl(course.getThumbnailUrl());
        response.setStatus(course.getStatus());
        response.setLessonCount(lessonCount);
        response.setCreatedAt(course.getCreatedAt());
        response.setUpdatedAt(course.getUpdatedAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public long getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(long lessonCount) {
        this.lessonCount = lessonCount;
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
