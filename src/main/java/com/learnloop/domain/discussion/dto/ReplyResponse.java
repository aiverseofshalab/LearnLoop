package com.learnloop.domain.discussion.dto;

import com.learnloop.domain.discussion.entity.Reply;

import java.time.LocalDateTime;

public class ReplyResponse {

    private String id;
    private String questionId;
    private String userId;
    private String userName;
    private String userRole;
    private String content;
    private boolean teacherReply;
    private LocalDateTime createdAt;

    public ReplyResponse() {
    }

    public static ReplyResponse fromEntity(Reply reply) {
        ReplyResponse response = new ReplyResponse();
        response.setId(reply.getId());
        response.setQuestionId(reply.getQuestion().getId());
        response.setUserId(reply.getUser().getId());
        response.setUserName(reply.getUser().getFullName());
        response.setUserRole(reply.getUser().getRole().name());
        response.setContent(reply.getContent());
        response.setTeacherReply(reply.isTeacherReply());
        response.setCreatedAt(reply.getCreatedAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isTeacherReply() {
        return teacherReply;
    }

    public void setTeacherReply(boolean teacherReply) {
        this.teacherReply = teacherReply;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
