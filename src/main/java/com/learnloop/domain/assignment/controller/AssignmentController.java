package com.learnloop.domain.assignment.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.assignment.dto.AssignmentResponse;
import com.learnloop.domain.assignment.dto.SubmissionRequest;
import com.learnloop.domain.assignment.dto.SubmissionResponse;
import com.learnloop.domain.assignment.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Assignments & Submissions", description = "Endpoints for viewing assignments and submitting coursework")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/courses/{courseId}/assignments")
    @Operation(summary = "Get all assignments for a course with student submission indicators")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getCourseAssignments(@PathVariable String courseId) {
        List<AssignmentResponse> assignments = assignmentService.getCourseAssignments(courseId);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @GetMapping("/assignments/{id}")
    @Operation(summary = "Get assignment instructions and student's submission status")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignment(@PathVariable String id) {
        AssignmentResponse assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(ApiResponse.success(assignment));
    }

    @PostMapping("/assignments/{id}/submissions")
    @Operation(summary = "Submit coursework for an assignment")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitAssignment(
            @PathVariable String id,
            @Valid @RequestBody SubmissionRequest request) {
        SubmissionResponse response = assignmentService.submitAssignment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment submitted successfully", response));
    }

    @GetMapping("/students/me/submissions")
    @Operation(summary = "Get list of submissions made by the currently authenticated student")
    public ResponseEntity<ApiResponse<Page<SubmissionResponse>>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SubmissionResponse> submissions = assignmentService.getMySubmissions(pageable);
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }
}
