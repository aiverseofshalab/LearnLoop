package com.learnloop.domain.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnloop.config.KafkaConfig;
import com.learnloop.domain.audit.service.AuditService;
import com.learnloop.domain.kafka.event.LessonCompletedEvent;
import com.learnloop.domain.outbox.service.IdempotencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LearningActivityConsumer {

    private static final Logger log = LoggerFactory.getLogger(LearningActivityConsumer.class);
    public static final String CONSUMER_GROUP = "learnloop-activity-consumer";

    private final ObjectMapper objectMapper;
    private final IdempotencyService idempotencyService;
    private final AuditService auditService;

    public LearningActivityConsumer(ObjectMapper objectMapper,
                                    IdempotencyService idempotencyService,
                                    AuditService auditService) {
        this.objectMapper = objectMapper;
        this.idempotencyService = idempotencyService;
        this.auditService = auditService;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_LEARNING_ACTIVITY, groupId = CONSUMER_GROUP)
    public void handleLessonCompletedEvent(String payload) {
        try {
            LessonCompletedEvent event = objectMapper.readValue(payload, LessonCompletedEvent.class);
            log.info("Received LessonCompletedEvent: eventId={}, studentId={}, lesson={}",
                    event.getEventId(), event.getStudentId(), event.getLessonTitle());

            boolean isNew = idempotencyService.recordProcessingIfNew(
                    event.getEventId(), CONSUMER_GROUP, event.getEventType());
            if (!isNew) {
                log.info("Skipping duplicate learning activity event {}", event.getEventId());
                return;
            }

            auditService.logAction(
                    event.getStudentId(),
                    "LESSON_COMPLETED",
                    "LESSON",
                    event.getLessonId(),
                    "Completed lesson '" + event.getLessonTitle() + "' in course '" + event.getCourseTitle() + "'",
                    "SYSTEM_KAFKA"
            );
        } catch (Exception e) {
            log.error("Failed to process learning activity event payload: {}", payload, e);
            throw new RuntimeException("Error processing learning activity event", e);
        }
    }
}
