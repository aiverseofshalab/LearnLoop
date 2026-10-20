package com.learnloop.domain.discussion.dto;

import jakarta.validation.constraints.NotBlank;

public class ReplyRequest {

    @NotBlank(message = "Reply content cannot be empty")
    private String content;

    public ReplyRequest() {
    }

    public ReplyRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
