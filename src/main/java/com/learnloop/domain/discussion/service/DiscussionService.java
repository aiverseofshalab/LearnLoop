package com.learnloop.domain.discussion.service;

import com.learnloop.common.exceptions.ForbiddenException;
import com.learnloop.common.exceptions.ResourceNotFoundException;
import com.learnloop.config.KafkaConfig;
import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.entity.CourseStatus;
import com.learnloop.domain.course.service.CourseService;
import com.learnloop.domain.discussion.dto.QuestionDetailResponse;
import com.learnloop.domain.discussion.dto.QuestionRequest;
import com.learnloop.domain.discussion.dto.QuestionResponse;
import com.learnloop.domain.discussion.dto.ReplyRequest;
import com.learnloop.domain.discussion.dto.ReplyResponse;
import com.learnloop.domain.discussion.dto.UpdateQuestionStatusRequest;
import com.learnloop.domain.discussion.entity.Question;
import com.learnloop.domain.discussion.entity.QuestionStatus;
import com.learnloop.domain.discussion.entity.Reply;
import com.learnloop.domain.discussion.repository.QuestionRepository;
import com.learnloop.domain.discussion.repository.ReplyRepository;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.kafka.event.DiscussionReplyEvent;
import com.learnloop.domain.lesson.entity.Lesson;
import com.learnloop.domain.lesson.repository.LessonRepository;
import com.learnloop.domain.outbox.service.OutboxService;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.service.UserService;
import com.learnloop.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiscussionService {

    private static final Logger log = LoggerFactory.getLogger(DiscussionService.class);

    private final QuestionRepository questionRepository;
    private final ReplyRepository replyRepository;
    private final CourseService courseService;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final OutboxService outboxService;

    public DiscussionService(QuestionRepository questionRepository,
                             ReplyRepository replyRepository,
                             CourseService courseService,
                             LessonRepository lessonRepository,
                             EnrollmentRepository enrollmentRepository,
                             UserService userService,
                             OutboxService outboxService) {
        this.questionRepository = questionRepository;
        this.replyRepository = replyRepository;
        this.courseService = courseService;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userService = userService;
        this.outboxService = outboxService;
    }

    @Transactional
    public QuestionResponse askQuestion(QuestionRequest request) {
        String studentId = SecurityUtils.getCurrentUserId();
        User student = userService.getUserEntity(studentId);
        Course course = courseService.getCourseEntity(request.getCourseId());

        if (course.getStatus() != CourseStatus.PUBLISHED && !course.getTeacher().getId().equals(studentId)) {
            throw new ForbiddenException("Cannot ask questions in an unpublished course");
        }

        // Must be enrolled, course teacher, or admin
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, course.getId());
        boolean isTeacher = course.getTeacher().getId().equals(studentId);
        boolean isAdmin = SecurityUtils.hasRole(UserRole.ADMIN);

        if (!isEnrolled && !isTeacher && !isAdmin) {
            throw new ForbiddenException("You must be enrolled in this course to participate in discussions");
        }

        Lesson lesson = null;
        if (request.getLessonId() != null && !request.getLessonId().trim().isEmpty()) {
            lesson = lessonRepository.findByIdAndCourseId(request.getLessonId().trim(), course.getId())
                    .orElse(null);
        }

        Question question = new Question(
                course,
                lesson,
                student,
                request.getTitle().trim(),
                request.getDescription().trim()
        );

        Question saved = questionRepository.save(question);
        log.info("Student {} posted new doubt {} in course {}", studentId, saved.getId(), course.getId());
        return QuestionResponse.fromEntity(saved, 0);
    }

    @Transactional(readOnly = true)
    public Page<QuestionResponse> searchQuestions(String courseId, QuestionStatus status, String query, Pageable pageable) {
        Page<Question> questions = questionRepository.searchQuestions(courseId, status, query, pageable);
        return questions.map(q -> {
            long replyCount = replyRepository.countByQuestionId(q.getId());
            return QuestionResponse.fromEntity(q, replyCount);
        });
    }

    @Transactional(readOnly = true)
    public QuestionDetailResponse getQuestionDetail(String questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        List<Reply> replies = replyRepository.findByQuestionIdWithUser(questionId);
        List<ReplyResponse> replyResponses = replies.stream()
                .map(ReplyResponse::fromEntity)
                .toList();

        return QuestionDetailResponse.fromEntity(question, replyResponses);
    }

    @Transactional
    public ReplyResponse addReply(String questionId, ReplyRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        User replier = userService.getUserEntity(currentUserId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        Course course = question.getCourse();
        boolean isCourseTeacher = course.getTeacher().getId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.hasRole(UserRole.ADMIN);
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(currentUserId, course.getId());

        if (!isCourseTeacher && !isAdmin && !isEnrolled && !question.getStudent().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to reply in this discussion");
        }

        boolean isTeacherReply = isCourseTeacher || isAdmin;
        Reply reply = new Reply(question, replier, request.getContent().trim(), isTeacherReply);
        Reply saved = replyRepository.save(reply);

        // If teacher replied and status was OPEN, update status to ANSWERED
        if (isTeacherReply && question.getStatus() == QuestionStatus.OPEN) {
            question.setStatus(QuestionStatus.ANSWERED);
            questionRepository.save(question);
        }

        // Publish Outbox Event for discussion reply
        DiscussionReplyEvent event = new DiscussionReplyEvent(
                question.getId(),
                question.getTitle(),
                question.getStudent().getId(),
                replier.getId(),
                replier.getFullName(),
                isTeacherReply,
                course.getId()
        );
        outboxService.saveEvent(KafkaConfig.TOPIC_DISCUSSION, event);

        log.info("User {} replied to question {}", currentUserId, questionId);
        return ReplyResponse.fromEntity(saved);
    }

    @Transactional
    public QuestionResponse updateQuestionStatus(String questionId, UpdateQuestionStatusRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAuthor = question.getStudent().getId().equals(currentUserId);
        boolean isTeacher = question.getCourse().getTeacher().getId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.hasRole(UserRole.ADMIN);

        if (!isAuthor && !isTeacher && !isAdmin) {
            throw new ForbiddenException("You do not have permission to change the status of this question");
        }

        question.setStatus(request.getStatus());
        Question saved = questionRepository.save(question);
        long replyCount = replyRepository.countByQuestionId(questionId);
        return QuestionResponse.fromEntity(saved, replyCount);
    }
}
