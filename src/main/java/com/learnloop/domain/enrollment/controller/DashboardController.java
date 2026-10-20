package com.learnloop.domain.enrollment.controller;

import com.learnloop.common.ApiResponse;
import com.learnloop.domain.enrollment.dto.AdminDashboardResponse;
import com.learnloop.domain.enrollment.dto.StudentDashboardResponse;
import com.learnloop.domain.enrollment.dto.TeacherDashboardResponse;
import com.learnloop.domain.enrollment.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Dashboards & Analytics", description = "Endpoints for role-specific dashboard metrics and platform statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/students/me/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get aggregated student dashboard metrics and course enrollments")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard() {
        StudentDashboardResponse dashboard = dashboardService.getStudentDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/teacher/analytics")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get teacher analytics, pending submissions, and doubt counts")
    public ResponseEntity<ApiResponse<TeacherDashboardResponse>> getTeacherDashboard() {
        TeacherDashboardResponse dashboard = dashboardService.getTeacherDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get platform-wide aggregate user, course, and operational statistics")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        AdminDashboardResponse dashboard = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
