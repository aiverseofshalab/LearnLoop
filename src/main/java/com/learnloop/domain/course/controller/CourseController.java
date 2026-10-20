package com.learnloop.domain.course.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.course.dto.CourseDetailResponse;
import com.learnloop.domain.course.dto.CourseResponse;
import com.learnloop.domain.course.entity.DifficultyLevel;
import com.learnloop.domain.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Courses", description = "Endpoints for public and student course discovery")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @Operation(summary = "Search and filter published courses with pagination")
    public ResponseEntity<ApiResponse<Page<CourseResponse>>> searchCourses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) DifficultyLevel difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CourseResponse> courses = courseService.searchPublishedCourses(query, subject, difficulty, pageable);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/subjects")
    @Operation(summary = "Get list of distinct published course subjects")
    public ResponseEntity<ApiResponse<List<String>>> getSubjects() {
        List<String> subjects = courseService.getAvailableSubjects();
        return ResponseEntity.ok(ApiResponse.success(subjects));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed course information, syllabus, and enrollment status")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(@PathVariable String id) {
        CourseDetailResponse course = courseService.getCourseDetail(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }
}
