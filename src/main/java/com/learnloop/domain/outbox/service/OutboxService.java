package com.learnloop.domain.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnloop.domain.kafka.event.BaseEvent;
import com.learnloop.domain.outbox.entity.OutboxEvent;
import com.learnloop.domain.outbox.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Saves an event to the outbox within the existing database transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveEvent(String topic, BaseEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(
                    event.getEventType(),
                    topic,
                    event.getAggregateId(),
                    payload
            );
            outboxEventRepository.save(outboxEvent);
            log.info("Saved outbox event: type={}, topic={}, aggregateId={}",
                    event.getEventType(), topic, event.getAggregateId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event to JSON: {}", event.getEventType(), e);
            throw new RuntimeException("Could not serialize outbox event", e);
        }
    }
}
