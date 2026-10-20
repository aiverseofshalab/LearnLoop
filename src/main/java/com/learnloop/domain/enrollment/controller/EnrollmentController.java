package com.learnloop.domain.enrollment.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.enrollment.dto.EnrollmentResponse;
import com.learnloop.domain.enrollment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Enrollments", description = "Endpoints for course enrollments and enrolled student lists")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/courses/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Enroll current student into a published course")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(@PathVariable String courseId) {
        EnrollmentResponse response = enrollmentService.enrollInCourse(courseId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Enrolled successfully", response));
    }

    @GetMapping("/students/me/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get all course enrollments and progress for the current student")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getMyEnrollments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments(pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/teacher/courses/{courseId}/enrollments")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get list of students enrolled in a teacher's course")
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getCourseEnrollments(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EnrollmentResponse> enrollments = enrollmentService.getTeacherCourseEnrollments(courseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }
}
