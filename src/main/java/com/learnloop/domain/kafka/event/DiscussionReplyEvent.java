package com.learnloop.domain.kafka.event;

public class DiscussionReplyEvent extends BaseEvent {

    private String questionId;
    private String questionTitle;
    private String questionAuthorId;
    private String replierId;
    private String replierName;
    private boolean teacherReply;
    private String courseId;

    public DiscussionReplyEvent() {
        super();
    }

    public DiscussionReplyEvent(String questionId, String questionTitle, String questionAuthorId,
                                String replierId, String replierName, boolean teacherReply, String courseId) {
        super("DISCUSSION_REPLIED", questionId);
        this.questionId = questionId;
        this.questionTitle = questionTitle;
        this.questionAuthorId = questionAuthorId;
        this.replierId = replierId;
        this.replierName = replierName;
        this.teacherReply = teacherReply;
        this.courseId = courseId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getQuestionAuthorId() {
        return questionAuthorId;
    }

    public void setQuestionAuthorId(String questionAuthorId) {
        this.questionAuthorId = questionAuthorId;
    }

    public String getReplierId() {
        return replierId;
    }

    public void setReplierId(String replierId) {
        this.replierId = replierId;
    }

    public String getReplierName() {
        return replierName;
    }

    public void setReplierName(String replierName) {
        this.replierName = replierName;
    }

    public boolean isTeacherReply() {
        return teacherReply;
    }

    public void setTeacherReply(boolean teacherReply) {
        this.teacherReply = teacherReply;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}
