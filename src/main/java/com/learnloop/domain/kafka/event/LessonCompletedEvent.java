package com.learnloop.domain.kafka.event;

public class LessonCompletedEvent extends BaseEvent {

    private String studentId;
    private String lessonId;
    private String lessonTitle;
    private String courseId;
    private String courseTitle;

    public LessonCompletedEvent() {
        super();
    }

    public LessonCompletedEvent(String studentId, String lessonId, String lessonTitle,
                                String courseId, String courseTitle) {
        super("LESSON_COMPLETED", lessonId);
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
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
}
