package com.learnloop.domain.enrollment.service;

import com.learnloop.common.exceptions.BadRequestException;
import com.learnloop.common.exceptions.DuplicateResourceException;
import com.learnloop.config.KafkaConfig;
import com.learnloop.domain.audit.service.AuditService;
import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.entity.CourseStatus;
import com.learnloop.domain.course.entity.DifficultyLevel;
import com.learnloop.domain.course.repository.CourseRepository;
import com.learnloop.domain.enrollment.dto.EnrollmentResponse;
import com.learnloop.domain.enrollment.entity.Enrollment;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.enrollment.repository.LessonProgressRepository;
import com.learnloop.domain.kafka.event.EnrollmentEvent;
import com.learnloop.domain.lesson.repository.LessonRepository;
import com.learnloop.domain.outbox.service.OutboxService;
import com.learnloop.domain.user.entity.User;
import com.learnloop.domain.user.entity.UserRole;
import com.learnloop.domain.user.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private LessonProgressRepository lessonProgressRepository;

    @Mock
    private OutboxService outboxService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User student;
    private User teacher;
    private Course publishedCourse;

    @BeforeEach
    void setUp() {
        student = new User("student@learnloop.com", "hash", "Bob Student", UserRole.STUDENT);
        teacher = new User("teacher@learnloop.com", "hash", "Prof. Smith", UserRole.TEACHER);
        publishedCourse = new Course("Java Mastery", "Short", "Detailed", "Computer Science",
                DifficultyLevel.INTERMEDIATE, teacher, "https://example.com/thumb.jpg");
        publishedCourse.setStatus(CourseStatus.PUBLISHED);

        UserPrincipal principal = UserPrincipal.create(student);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Student successfully enrolls in course and saves Transactional Outbox event")
    void enrollInCourse_Success_SavesOutboxEvent() {
        when(courseRepository.findById(publishedCourse.getId())).thenReturn(Optional.of(publishedCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), publishedCourse.getId())).thenReturn(false);
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));

        Enrollment enrollment = new Enrollment(student, publishedCourse);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse response = enrollmentService.enrollInCourse(publishedCourse.getId());

        assertNotNull(response);
        assertEquals(publishedCourse.getId(), response.getCourseId());


        ArgumentCaptor<EnrollmentEvent> eventCaptor = ArgumentCaptor.forClass(EnrollmentEvent.class);
        verify(outboxService).saveEvent(eq(KafkaConfig.TOPIC_ENROLLMENT), eventCaptor.capture());
        assertEquals(student.getId(), eventCaptor.getValue().getStudentId());
        assertEquals(publishedCourse.getTitle(), eventCaptor.getValue().getCourseTitle());
    }

    @Test
    @DisplayName("Duplicate enrollment attempt throws DuplicateResourceException")
    void enrollInCourse_Duplicate_ThrowsException() {
        when(courseRepository.findById(publishedCourse.getId())).thenReturn(Optional.of(publishedCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), publishedCourse.getId())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> enrollmentService.enrollInCourse(publishedCourse.getId()));
    }

    @Test
    @DisplayName("Attempting to enroll in an unpublished course throws BadRequestException")
    void enrollInCourse_Unpublished_ThrowsBadRequest() {
        publishedCourse.setStatus(CourseStatus.DRAFT);
        when(courseRepository.findById(publishedCourse.getId())).thenReturn(Optional.of(publishedCourse));

        assertThrows(BadRequestException.class, () -> enrollmentService.enrollInCourse(publishedCourse.getId()));
    }
}
