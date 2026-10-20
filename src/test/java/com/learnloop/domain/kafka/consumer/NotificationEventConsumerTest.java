package com.learnloop.domain.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnloop.domain.kafka.event.AssignmentGradedEvent;
import com.learnloop.domain.kafka.event.DiscussionReplyEvent;
import com.learnloop.domain.kafka.event.EnrollmentEvent;
import com.learnloop.domain.notification.entity.NotificationType;
import com.learnloop.domain.notification.service.NotificationService;
import com.learnloop.domain.outbox.service.IdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private NotificationService notificationService;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private NotificationEventConsumer consumer;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Enrollment event triggers creation of in-app notification for the student")
    void handleEnrollmentEvent_CreatesNotification() throws Exception {
        EnrollmentEvent event = new EnrollmentEvent("stud-1", "Alice", "alice@example.com", "course-101", "Spring Boot Pro");
        String jsonPayload = objectMapper.writeValueAsString(event);

        when(idempotencyService.recordProcessingIfNew(eq(event.getEventId()), eq(NotificationEventConsumer.CONSUMER_GROUP), any()))
                .thenReturn(true);

        consumer.handleEnrollmentEvent(jsonPayload);

        verify(notificationService).createNotification(
                eq("stud-1"),
                eq("Enrollment Confirmed!"),
                contains("Spring Boot Pro"),
                eq(NotificationType.ENROLLMENT),
                eq("COURSE"),
                eq("course-101")
        );
    }

    @Test
    @DisplayName("Duplicate enrollment event is skipped by consumer")
    void handleEnrollmentEvent_Duplicate_Skipped() throws Exception {
        EnrollmentEvent event = new EnrollmentEvent("stud-1", "Alice", "alice@example.com", "course-101", "Spring Boot Pro");
        String jsonPayload = objectMapper.writeValueAsString(event);

        when(idempotencyService.recordProcessingIfNew(eq(event.getEventId()), eq(NotificationEventConsumer.CONSUMER_GROUP), any()))
                .thenReturn(false);

        consumer.handleEnrollmentEvent(jsonPayload);

        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Discussion reply event triggers notification for question author")
    void handleDiscussionReplyEvent_CreatesNotification() throws Exception {
        DiscussionReplyEvent event = new DiscussionReplyEvent(
                "q-1", "How does Kafka work?", "student-author", "teacher-replier", "Prof. Dave", true, "course-101"
        );
        String jsonPayload = objectMapper.writeValueAsString(event);

        when(idempotencyService.recordProcessingIfNew(eq(event.getEventId()), eq(NotificationEventConsumer.CONSUMER_GROUP), any()))
                .thenReturn(true);

        consumer.handleDiscussionReplyEvent(jsonPayload);

        verify(notificationService).createNotification(
                eq("student-author"),
                contains("Instructor Answered Your Doubt"),
                contains("Prof. Dave replied"),
                eq(NotificationType.DISCUSSION_REPLY),
                eq("QUESTION"),
                eq("q-1")
        );
    }

    @Test
    @DisplayName("Assignment graded event triggers notification for student")
    void handleAssignmentGradedEvent_CreatesNotification() throws Exception {
        AssignmentGradedEvent event = new AssignmentGradedEvent(
                "assign-1", "Final Project", "student-1", new BigDecimal("98.50"), new BigDecimal("100.00"), "teacher-1", "course-101"
        );
        String jsonPayload = objectMapper.writeValueAsString(event);

        when(idempotencyService.recordProcessingIfNew(eq(event.getEventId()), eq(NotificationEventConsumer.CONSUMER_GROUP), any()))
                .thenReturn(true);

        consumer.handleAssignmentGradedEvent(jsonPayload);

        verify(notificationService).createNotification(
                eq("student-1"),
                contains("Assignment Graded"),
                contains("Score: 98.50 / 100.00"),
                eq(NotificationType.ASSIGNMENT_GRADED),
                eq("ASSIGNMENT"),
                eq("assign-1")
        );
    }
}
