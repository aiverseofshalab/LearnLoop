package com.learnloop.domain.discussion.dto;

import com.learnloop.domain.discussion.entity.QuestionStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateQuestionStatusRequest {

    @NotNull(message = "Question status is required")
    private QuestionStatus status;

    public UpdateQuestionStatusRequest() {
    }

    public UpdateQuestionStatusRequest(QuestionStatus status) {
        this.status = status;
    }

    public QuestionStatus getStatus() {
        return status;
    }

    public void setStatus(QuestionStatus status) {
        this.status = status;
    }
}
