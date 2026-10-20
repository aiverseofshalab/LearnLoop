package com.learnloop.domain.lesson.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.lesson.dto.LessonRequest;
import com.learnloop.domain.lesson.dto.LessonResponse;
import com.learnloop.domain.lesson.dto.ReorderLessonsRequest;
import com.learnloop.domain.lesson.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teacher")
@PreAuthorize("hasRole('TEACHER')")
@Tag(name = "Teacher Lessons", description = "Endpoints for teachers to manage lesson contents and ordering")
public class TeacherLessonController {

    private final LessonService lessonService;

    public TeacherLessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/courses/{courseId}/lessons")
    @Operation(summary = "Add a new lesson to an owned course")
    public ResponseEntity<ApiResponse<LessonResponse>> addLesson(
            @PathVariable String courseId,
            @Valid @RequestBody LessonRequest request) {
        LessonResponse response = lessonService.addLesson(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lesson added successfully", response));
    }

    @PatchMapping("/lessons/{lessonId}")
    @Operation(summary = "Update an existing lesson")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @PathVariable String lessonId,
            @Valid @RequestBody LessonRequest request) {
        LessonResponse updated = lessonService.updateLesson(lessonId, request);
        return ResponseEntity.ok(ApiResponse.success("Lesson updated successfully", updated));
    }

    @DeleteMapping("/lessons/{lessonId}")
    @Operation(summary = "Delete a lesson from an owned course")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable String lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.successMessage("Lesson deleted successfully"));
    }

    @PostMapping("/courses/{courseId}/lessons/reorder")
    @Operation(summary = "Reorder lessons within an owned course")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> reorderLessons(
            @PathVariable String courseId,
            @Valid @RequestBody ReorderLessonsRequest request) {
        List<LessonResponse> reordered = lessonService.reorderLessons(courseId, request);
        return ResponseEntity.ok(ApiResponse.success("Lessons reordered successfully", reordered));
    }
}
