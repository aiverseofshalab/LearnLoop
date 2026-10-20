package com.learnloop.domain.outbox.service;

import com.learnloop.domain.outbox.entity.ProcessedEvent;
import com.learnloop.domain.outbox.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final ProcessedEventRepository processedEventRepository;

    public IdempotencyService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(String eventId, String consumerGroup) {
        return processedEventRepository.existsByEventIdAndConsumerGroup(eventId, consumerGroup);
    }

    @Transactional
    public boolean recordProcessingIfNew(String eventId, String consumerGroup, String eventType) {
        if (processedEventRepository.existsByEventIdAndConsumerGroup(eventId, consumerGroup)) {
            log.info("Event {} has already been processed by consumer group {}. Skipping duplicate.",
                    eventId, consumerGroup);
            return false;
        }

        try {
            ProcessedEvent record = new ProcessedEvent(eventId, consumerGroup, eventType);
            processedEventRepository.save(record);
            return true;
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent duplicate event detected for eventId {} in group {}. Skipping.",
                    eventId, consumerGroup);
            return false;
        }
    }
}
