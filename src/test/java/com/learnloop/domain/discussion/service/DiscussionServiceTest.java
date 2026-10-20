package com.learnloop.domain.discussion.service;

import com.learnloop.config.KafkaConfig;
import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.entity.CourseStatus;
import com.learnloop.domain.course.entity.DifficultyLevel;
import com.learnloop.domain.course.service.CourseService;
import com.learnloop.domain.discussion.dto.QuestionRequest;
import com.learnloop.domain.discussion.dto.QuestionResponse;
import com.learnloop.domain.discussion.dto.ReplyRequest;
import com.learnloop.domain.discussion.dto.ReplyResponse;
import com.learnloop.domain.discussion.entity.Question;
import com.learnloop.domain.discussion.entity.QuestionStatus;
import com.learnloop.domain.discussion.entity.Reply;
import com.learnloop.domain.discussion.repository.QuestionRepository;
import com.learnloop.domain.discussion.repository.ReplyRepository;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.kafka.event.DiscussionReplyEvent;
import com.learnloop.domain.lesson.repository.LessonRepository;
import com.learnloop.domain.outbox.service.OutboxService;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.service.UserService;
import com.learnloop.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscussionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserService userService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private DiscussionService discussionService;

    private User student;
    private User teacher;
    private Course course;
    private Question question;

    @BeforeEach
    void setUp() {
        student = new User("student@learnloop.com", "hash", "Bob Student", UserRole.STUDENT);
        teacher = new User("teacher@learnloop.com", "hash", "Prof. Alan", UserRole.TEACHER);
        course = new Course("Data Structures", "Short", "Detailed", "CS", DifficultyLevel.BEGINNER, teacher, null);
        course.setStatus(CourseStatus.PUBLISHED);

        question = new Question(course, null, student, "How does QuickSort pivot work?", "Explanation needed");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Enrolled student asks doubt successfully")
    void askQuestion_Success() {
        UserPrincipal principal = UserPrincipal.create(student);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        QuestionRequest request = new QuestionRequest(course.getId(), null, "How does QuickSort pivot work?", "Explanation needed");

        when(userService.getUserEntity(student.getId())).thenReturn(student);
        when(courseService.getCourseEntity(course.getId())).thenReturn(course);
        when(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())).thenReturn(true);
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        QuestionResponse response = discussionService.askQuestion(request);

        assertNotNull(response);
        assertEquals("How does QuickSort pivot work?", response.getTitle());
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("Teacher answers doubt, auto-updates question to ANSWERED, and saves Outbox event")
    void replyToQuestion_ByTeacher_UpdatesStatusAndSavesOutbox() {
        UserPrincipal teacherPrincipal = UserPrincipal.create(teacher);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(teacherPrincipal, null, teacherPrincipal.getAuthorities()));

        ReplyRequest request = new ReplyRequest("Pivot partitions elements smaller to left and larger to right.");

        when(userService.getUserEntity(teacher.getId())).thenReturn(teacher);
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));

        Reply reply = new Reply(question, teacher, request.getContent(), true);
        when(replyRepository.save(any(Reply.class))).thenReturn(reply);

        ReplyResponse response = discussionService.addReply(question.getId(), request);

        assertNotNull(response);
        assertTrue(response.isTeacherReply());
        assertEquals(QuestionStatus.ANSWERED, question.getStatus());

        ArgumentCaptor<DiscussionReplyEvent> eventCaptor = ArgumentCaptor.forClass(DiscussionReplyEvent.class);
        verify(outboxService).saveEvent(eq(KafkaConfig.TOPIC_DISCUSSION), eventCaptor.capture());
        assertEquals(student.getId(), eventCaptor.getValue().getQuestionAuthorId());
        assertTrue(eventCaptor.getValue().isTeacherReply());
    }
}
