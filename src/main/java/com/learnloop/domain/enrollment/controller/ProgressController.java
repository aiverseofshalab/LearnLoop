package com.learnloop.domain.enrollment.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.enrollment.dto.StudentProgressResponse;
import com.learnloop.domain.enrollment.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Learning Progress", description = "Endpoints for tracking lesson completions and course progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Mark an eligible lesson as completed by the student")
    public ResponseEntity<ApiResponse<StudentProgressResponse>> markLessonComplete(@PathVariable String lessonId) {
        StudentProgressResponse progress = progressService.markLessonComplete(lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lesson marked as complete", progress));
    }

    @GetMapping("/courses/{courseId}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get progress percentage and completed counts for a course")
    public ResponseEntity<ApiResponse<StudentProgressResponse>> getCourseProgress(@PathVariable String courseId) {
        StudentProgressResponse progress = progressService.getCourseProgress(courseId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
}
