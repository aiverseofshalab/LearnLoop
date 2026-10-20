package com.learnloop.domain.enrollment.dto;

public class StudentProgressResponse {

    private String courseId;
    private String courseTitle;
    private long completedLessons;
    private long totalLessons;
    private double progressPercentage;

    public StudentProgressResponse() {
    }

    public StudentProgressResponse(String courseId, String courseTitle, long completedLessons, long totalLessons, double progressPercentage) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.completedLessons = completedLessons;
        this.totalLessons = totalLessons;
        this.progressPercentage = progressPercentage;
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

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}
