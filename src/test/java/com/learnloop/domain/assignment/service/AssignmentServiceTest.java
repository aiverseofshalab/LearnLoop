package com.learnloop.domain.assignment.service;

import com.learnloop.common.exceptions.BadRequestException;
import com.learnloop.config.KafkaConfig;
import com.learnloop.domain.assignment.dto.GradeSubmissionRequest;
import com.learnloop.domain.assignment.dto.SubmissionRequest;
import com.learnloop.domain.assignment.dto.SubmissionResponse;
import com.learnloop.domain.assignment.entity.Assignment;
import com.learnloop.domain.assignment.entity.Submission;
import com.learnloop.domain.assignment.entity.SubmissionStatus;
import com.learnloop.domain.assignment.repository.AssignmentRepository;
import com.learnloop.domain.assignment.repository.SubmissionRepository;
import com.learnloop.domain.course.entity.Course;
import com.learnloop.domain.course.entity.DifficultyLevel;
import com.learnloop.domain.course.service.CourseService;
import com.learnloop.domain.enrollment.repository.EnrollmentRepository;
import com.learnloop.domain.kafka.event.AssignmentGradedEvent;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private CourseService courseService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserService userService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private AssignmentService assignmentService;

    private User student;
    private User teacher;
    private Course course;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        student = new User("student@learnloop.com", "hash", "Bob Student", UserRole.STUDENT);
        teacher = new User("teacher@learnloop.com", "hash", "Prof. Alan", UserRole.TEACHER);
        course = new Course("Algorithms", "Short", "Detailed", "CS", DifficultyLevel.ADVANCED, teacher, null);
        assignment = new Assignment(course, teacher, "Sorting Benchmarks", "Implement merge sort",
                new BigDecimal("100.00"), LocalDateTime.now().plusDays(7));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Enrolled student submits assignment response successfully")
    void submitAssignment_Success() {
        UserPrincipal principal = UserPrincipal.create(student);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        SubmissionRequest request = new SubmissionRequest("Here is my code implementation", "https://github.com/myrepo");

        when(userService.getUserEntity(student.getId())).thenReturn(student);
        when(assignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())).thenReturn(true);
        when(submissionRepository.findByAssignmentIdAndStudentId(assignment.getId(), student.getId()))
                .thenReturn(Optional.empty());

        Submission submission = new Submission(assignment, student, request.getTextContent(), request.getResourceUrl());
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);

        SubmissionResponse response = assignmentService.submitAssignment(assignment.getId(), request);

        assertNotNull(response);
        assertEquals(SubmissionStatus.SUBMITTED, response.getStatus());
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    @DisplayName("Teacher grades submission and publishes AssignmentGradedEvent to Outbox")
    void gradeSubmission_Success_SavesOutboxEvent() {
        UserPrincipal teacherPrincipal = UserPrincipal.create(teacher);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(teacherPrincipal, null, teacherPrincipal.getAuthorities()));

        Submission submission = new Submission(assignment, student, "Student solution", null);
        GradeSubmissionRequest request = new GradeSubmissionRequest(new BigDecimal("95.00"), "Great work on recursion base case!");

        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        when(courseService.getCourseWithOwnershipCheck(course.getId())).thenReturn(course);
        when(userService.getUserEntity(teacher.getId())).thenReturn(teacher);
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);

        SubmissionResponse response = assignmentService.gradeSubmission(submission.getId(), request);

        assertNotNull(response);
        assertEquals(SubmissionStatus.GRADED, response.getStatus());
        assertEquals(new BigDecimal("95.00"), response.getMarksObtained());

        ArgumentCaptor<AssignmentGradedEvent> eventCaptor = ArgumentCaptor.forClass(AssignmentGradedEvent.class);
        verify(outboxService).saveEvent(eq(KafkaConfig.TOPIC_ASSIGNMENT), eventCaptor.capture());
        assertEquals(student.getId(), eventCaptor.getValue().getStudentId());
        assertEquals(new BigDecimal("95.00"), eventCaptor.getValue().getMarksObtained());
    }

    @Test
    @DisplayName("Attempting to award marks exceeding max marks throws BadRequestException")
    void gradeSubmission_ExceedingMaxMarks_ThrowsBadRequest() {
        UserPrincipal teacherPrincipal = UserPrincipal.create(teacher);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(teacherPrincipal, null, teacherPrincipal.getAuthorities()));

        Submission submission = new Submission(assignment, student, "Student solution", null);
        GradeSubmissionRequest request = new GradeSubmissionRequest(new BigDecimal("105.00"), "Bonus marks");

        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        when(courseService.getCourseWithOwnershipCheck(course.getId())).thenReturn(course);
        when(userService.getUserEntity(teacher.getId())).thenReturn(teacher);

        assertThrows(BadRequestException.class, () -> assignmentService.gradeSubmission(submission.getId(), request));
    }
}
