package com.learnloop.domain.lesson.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.lesson.dto.LessonResponse;
import com.learnloop.domain.lesson.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Lessons", description = "Endpoints for accessing lesson content in enrolled courses")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/courses/{courseId}/lessons")
    @Operation(summary = "Get all accessible lessons for an enrolled course")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getCourseLessons(@PathVariable String courseId) {
        List<LessonResponse> lessons = lessonService.getLessonsForCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }

    @GetMapping("/lessons/{lessonId}")
    @Operation(summary = "Get single lesson details and content with access verification")
    public ResponseEntity<ApiResponse<LessonResponse>> getLesson(@PathVariable String lessonId) {
        LessonResponse lesson = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(ApiResponse.success(lesson));
    }
}
