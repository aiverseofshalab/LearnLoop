package com.learnloop.domain.course.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.course.dto.CourseRequest;
import com.learnloop.domain.course.dto.CourseResponse;
import com.learnloop.domain.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teacher/courses")
@PreAuthorize("hasRole('TEACHER')")
@Tag(name = "Teacher Courses", description = "Endpoints for teachers to create and manage their courses")
public class TeacherCourseController {

    private final CourseService courseService;

    public TeacherCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @Operation(summary = "Create a new course in DRAFT status")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", response));
    }

    @GetMapping
    @Operation(summary = "List courses owned by the authenticated teacher")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> getMyCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CourseResponse> courses = courseService.getTeacherCourses(pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update course details for an owned course")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable String id,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse updated = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updated));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish course to make it visible to students")
    public ResponseEntity<ApiResponse<CourseResponse>> publishCourse(@PathVariable String id) {
        CourseResponse published = courseService.publishCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course published successfully", published));
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "Unpublish course back to DRAFT state")
    public ResponseEntity<ApiResponse<CourseResponse>> unpublishCourse(@PathVariable String id) {
        CourseResponse unpublished = courseService.unpublishCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course unpublished successfully", unpublished));
    }
}
