package com.learnloop.domain.discussion.dto;

import com.learnloop.domain.discussion.entity.Question;
import com.learnloop.domain.discussion.entity.QuestionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionDetailResponse {

    private String id;
    private String courseId;
    private String courseTitle;
    private String lessonId;
    private String lessonTitle;
    private String studentId;
    private String studentName;
    private String title;
    private String description;
    private QuestionStatus status;
    private List<ReplyResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QuestionDetailResponse() {
    }

    public static QuestionDetailResponse fromEntity(Question q, List<ReplyResponse> replies) {
        QuestionDetailResponse response = new QuestionDetailResponse();
        response.setId(q.getId());
        response.setCourseId(q.getCourse().getId());
        response.setCourseTitle(q.getCourse().getTitle());
        if (q.getLesson() != null) {
            response.setLessonId(q.getLesson().getId());
            response.setLessonTitle(q.getLesson().getTitle());
        }
        response.setStudentId(q.getStudent().getId());
        response.setStudentName(q.getStudent().getFullName());
        response.setTitle(q.getTitle());
        response.setDescription(q.getDescription());
        response.setStatus(q.getStatus());
        response.setReplies(replies);
        response.setCreatedAt(q.getCreatedAt());
        response.setUpdatedAt(q.getUpdatedAt());
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

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
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

    public QuestionStatus getStatus() {
        return status;
    }

    public void setStatus(QuestionStatus status) {
        this.status = status;
    }

    public List<ReplyResponse> getReplies() {
        return replies;
    }

    public void setReplies(List<ReplyResponse> replies) {
        this.replies = replies;
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
