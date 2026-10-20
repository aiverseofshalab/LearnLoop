package com.learnloop.domain.enrollment.dto;

import com.learnloop.domain.enrollment.entity.Enrollment;
import com.learnloop.domain.enrollment.entity.EnrollmentStatus;

import java.time.LocalDateTime;

public class EnrollmentResponse {

    private String id;
    private String courseId;
    private String courseTitle;
    private String courseSubject;
    private String courseThumbnailUrl;
    private String teacherName;
    private EnrollmentStatus status;
    private double progressPercentage;
    private long completedLessons;
    private long totalLessons;
    private LocalDateTime enrolledAt;

    public EnrollmentResponse() {
    }

    public static EnrollmentResponse fromEntity(Enrollment enrollment, double progressPercentage,
                                                long completedLessons, long totalLessons) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setCourseId(enrollment.getCourse().getId());
        response.setCourseTitle(enrollment.getCourse().getTitle());
        response.setCourseSubject(enrollment.getCourse().getSubject());
        response.setCourseThumbnailUrl(enrollment.getCourse().getThumbnailUrl());
        response.setTeacherName(enrollment.getCourse().getTeacher().getFullName());
        response.setStatus(enrollment.getStatus());
        response.setProgressPercentage(progressPercentage);
        response.setCompletedLessons(completedLessons);
        response.setTotalLessons(totalLessons);
        response.setEnrolledAt(enrollment.getEnrolledAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseSubject() {
        return courseSubject;
    }

    public void setCourseSubject(String courseSubject) {
        this.courseSubject = courseSubject;
    }

    public String getCourseThumbnailUrl() {
        return courseThumbnailUrl;
    }

    public void setCourseThumbnailUrl(String courseThumbnailUrl) {
        this.courseThumbnailUrl = courseThumbnailUrl;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public long getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(long completedLessons) {
        this.completedLessons = completedLessons;
    }

    public long getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(long totalLessons) {
        this.totalLessons = totalLessons;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}
