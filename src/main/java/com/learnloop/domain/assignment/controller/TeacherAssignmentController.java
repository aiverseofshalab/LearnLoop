package com.learnloop.domain.assignment.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.assignment.dto.AssignmentRequest;
import com.learnloop.domain.assignment.dto.AssignmentResponse;
import com.learnloop.domain.assignment.dto.GradeSubmissionRequest;
import com.learnloop.domain.assignment.dto.SubmissionResponse;
import com.learnloop.domain.assignment.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
@Tag(name = "Teacher Assignments", description = "Endpoints for teachers to create assignments and grade student submissions")
public class TeacherAssignmentController {

    private final AssignmentService assignmentService;

    public TeacherAssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/courses/{courseId}/assignments")
    @Operation(summary = "Create an assignment for an owned course")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @PathVariable String courseId,
            @Valid @RequestBody AssignmentRequest request) {
        AssignmentResponse response = assignmentService.createAssignment(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assignment created successfully", response));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    @Operation(summary = "View all student submissions for an assignment")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissions(@PathVariable String assignmentId) {
        List<SubmissionResponse> submissions = assignmentService.getTeacherSubmissions(assignmentId);
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }

    @PatchMapping("/submissions/{submissionId}/grade")
    @Operation(summary = "Grade student submission and provide feedback")
    public ResponseEntity<ApiResponse<SubmissionResponse>> gradeSubmission(
            @PathVariable String submissionId,
            @Valid @RequestBody GradeSubmissionRequest request) {
        SubmissionResponse response = assignmentService.gradeSubmission(submissionId, request);
        return ResponseEntity.ok(ApiResponse.success("Submission graded successfully", response));
    }
}
