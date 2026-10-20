package com.learnloop.domain.lesson.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ReorderLessonsRequest {

    @NotEmpty(message = "Lesson order list cannot be empty")
    private List<String> lessonIds;

    public ReorderLessonsRequest() {
    }

    public ReorderLessonsRequest(List<String> lessonIds) {
        this.lessonIds = lessonIds;
    }

    public List<String> getLessonIds() {
        return lessonIds;
    }

    public void setLessonIds(List<String> lessonIds) {
        this.lessonIds = lessonIds;
    }
}
