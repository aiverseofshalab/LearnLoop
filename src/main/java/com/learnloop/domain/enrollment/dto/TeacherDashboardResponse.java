package com.learnloop.domain.enrollment.dto;

import com.learnloop.domain.course.dto.CourseResponse;

import java.util.List;

public class TeacherDashboardResponse {

    private long totalCourses;
    private long publishedCourses;
    private long draftCourses;
    private long totalEnrollments;
    private long unansweredQuestionsCount;
    private long pendingSubmissionsCount;
    private List<CourseResponse> recentCourses;

    public TeacherDashboardResponse() {
    }

    public long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public long getPublishedCourses() {
        return publishedCourses;
    }

    public void setPublishedCourses(long publishedCourses) {
        this.publishedCourses = publishedCourses;
    }

    public long getDraftCourses() {
        return draftCourses;
    }

    public void setDraftCourses(long draftCourses) {
        this.draftCourses = draftCourses;
    }

    public long getTotalEnrollments() {
        return totalEnrollments;
    }

    public void setTotalEnrollments(long totalEnrollments) {
        this.totalEnrollments = totalEnrollments;
    }

    public long getUnansweredQuestionsCount() {
        return unansweredQuestionsCount;
    }

    public void setUnansweredQuestionsCount(long unansweredQuestionsCount) {
        this.unansweredQuestionsCount = unansweredQuestionsCount;
    }

    public long getPendingSubmissionsCount() {
        return pendingSubmissionsCount;
    }

    public void setPendingSubmissionsCount(long pendingSubmissionsCount) {
        this.pendingSubmissionsCount = pendingSubmissionsCount;
    }

    public List<CourseResponse> getRecentCourses() {
        return recentCourses;
    }

    public void setRecentCourses(List<CourseResponse> recentCourses) {
        this.recentCourses = recentCourses;
    }
}
