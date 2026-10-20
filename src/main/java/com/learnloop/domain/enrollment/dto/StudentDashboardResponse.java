package com.learnloop.domain.enrollment.dto;

import java.util.List;

public class StudentDashboardResponse {

    private List<EnrollmentResponse> enrolledCourses;
    private double overallProgressPercentage;
    private long totalEnrolledCourses;
    private long totalCompletedLessons;
    private long unreadNotificationsCount;

    public StudentDashboardResponse() {
    }

    public List<EnrollmentResponse> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<EnrollmentResponse> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public double getOverallProgressPercentage() {
        return overallProgressPercentage;
    }

    public void setOverallProgressPercentage(double overallProgressPercentage) {
        this.overallProgressPercentage = overallProgressPercentage;
    }

    public long getTotalEnrolledCourses() {
        return totalEnrolledCourses;
    }

    public void setTotalEnrolledCourses(long totalEnrolledCourses) {
        this.totalEnrolledCourses = totalEnrolledCourses;
    }

    public long getTotalCompletedLessons() {
        return totalCompletedLessons;
    }

    public void setTotalCompletedLessons(long totalCompletedLessons) {
        this.totalCompletedLessons = totalCompletedLessons;
    }

    public long getUnreadNotificationsCount() {
        return unreadNotificationsCount;
    }

    public void setUnreadNotificationsCount(long unreadNotificationsCount) {
        this.unreadNotificationsCount = unreadNotificationsCount;
    }
}
