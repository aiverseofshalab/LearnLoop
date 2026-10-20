package com.learnloop.domain.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnloop.config.KafkaConfig;
import com.learnloop.domain.kafka.event.AssignmentGradedEvent;
import com.learnloop.domain.kafka.event.DiscussionReplyEvent;
import com.learnloop.domain.kafka.event.EnrollmentEvent;
import com.learnloop.domain.notification.entity.NotificationType;
import com.learnloop.domain.notification.service.NotificationService;
import com.learnloop.domain.outbox.service.IdempotencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);
    public static final String CONSUMER_GROUP = "learnloop-notification-consumer";

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final IdempotencyService idempotencyService;

    public NotificationEventConsumer(ObjectMapper objectMapper,
                                     NotificationService notificationService,
                                     IdempotencyService idempotencyService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.idempotencyService = idempotencyService;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_ENROLLMENT, groupId = CONSUMER_GROUP)
    public void handleEnrollmentEvent(String payload) {
        try {
            EnrollmentEvent event = objectMapper.readValue(payload, EnrollmentEvent.class);
            log.info("Received EnrollmentEvent: eventId={}, studentId={}, course={}",
                    event.getEventId(), event.getStudentId(), event.getCourseTitle());

            boolean isNew = idempotencyService.recordProcessingIfNew(
                    event.getEventId(), CONSUMER_GROUP, event.getEventType());
            if (!isNew) {
                log.info("Skipping duplicate enrollment event {}", event.getEventId());
                return;
            }

            notificationService.createNotification(
                    event.getStudentId(),
                    "Enrollment Confirmed!",
                    "You have successfully enrolled in " + event.getCourseTitle() + ". Start learning now!",
                    NotificationType.ENROLLMENT,
                    "COURSE",
                    event.getCourseId()
            );
        } catch (Exception e) {
            log.error("Failed to process enrollment event payload: {}", payload, e);
            throw new RuntimeException("Error processing enrollment event", e);
        }
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_DISCUSSION, groupId = CONSUMER_GROUP)
    public void handleDiscussionReplyEvent(String payload) {
        try {
            DiscussionReplyEvent event = objectMapper.readValue(payload, DiscussionReplyEvent.class);
            log.info("Received DiscussionReplyEvent: eventId={}, questionId={}, authorId={}",
                    event.getEventId(), event.getQuestionId(), event.getQuestionAuthorId());

            // Avoid sending notification if question author replied to their own question
            if (event.getQuestionAuthorId().equals(event.getReplierId())) {
                log.info("Author replied to own question. Skipping notification.");
                return;
            }

            boolean isNew = idempotencyService.recordProcessingIfNew(
                    event.getEventId(), CONSUMER_GROUP, event.getEventType());
            if (!isNew) {
                log.info("Skipping duplicate discussion reply event {}", event.getEventId());
                return;
            }

            String title = event.isTeacherReply() ? "Instructor Answered Your Doubt!" : "New Discussion Reply";
            String message = event.getReplierName() + " replied to your question: \"" + event.getQuestionTitle() + "\"";

            notificationService.createNotification(
                    event.getQuestionAuthorId(),
                    title,
                    message,
                    NotificationType.DISCUSSION_REPLY,
                    "QUESTION",
                    event.getQuestionId()
            );
        } catch (Exception e) {
            log.error("Failed to process discussion reply event payload: {}", payload, e);
            throw new RuntimeException("Error processing discussion reply event", e);
        }
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_ASSIGNMENT, groupId = CONSUMER_GROUP)
    public void handleAssignmentGradedEvent(String payload) {
        try {
            AssignmentGradedEvent event = objectMapper.readValue(payload, AssignmentGradedEvent.class);
            log.info("Received AssignmentGradedEvent: eventId={}, assignmentId={}, studentId={}",
                    event.getEventId(), event.getAssignmentId(), event.getStudentId());

            boolean isNew = idempotencyService.recordProcessingIfNew(
                    event.getEventId(), CONSUMER_GROUP, event.getEventType());
            if (!isNew) {
                log.info("Skipping duplicate assignment graded event {}", event.getEventId());
                return;
            }

            String title = "Assignment Graded: " + event.getAssignmentTitle();
            String message = "Your submission has been evaluated. Score: " + event.getMarksObtained() + " / " + event.getMaxMarks();

            notificationService.createNotification(
                    event.getStudentId(),
                    title,
                    message,
                    NotificationType.ASSIGNMENT_GRADED,
                    "ASSIGNMENT",
                    event.getAssignmentId()
            );
        } catch (Exception e) {
            log.error("Failed to process assignment graded event payload: {}", payload, e);
            throw new RuntimeException("Error processing assignment graded event", e);
        }
    }
}
