package com.learnloop.domain.outbox.service;

import com.learnloop.domain.outbox.entity.ProcessedEvent;
import com.learnloop.domain.outbox.repository.ProcessedEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    @DisplayName("New event is recorded successfully for the consumer group")
    void recordProcessingIfNew_NewEvent_ReturnsTrue() {
        String eventId = "evt-12345";
        String consumerGroup = "notification-group";
        String eventType = "ENROLLMENT_CONFIRMED";

        when(processedEventRepository.existsByEventIdAndConsumerGroup(eventId, consumerGroup)).thenReturn(false);

        boolean isNew = idempotencyService.recordProcessingIfNew(eventId, consumerGroup, eventType);

        assertTrue(isNew);
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    @DisplayName("Duplicate event delivery is detected and skipped")
    void recordProcessingIfNew_DuplicateEvent_ReturnsFalse() {
        String eventId = "evt-12345";
        String consumerGroup = "notification-group";
        String eventType = "ENROLLMENT_CONFIRMED";

        when(processedEventRepository.existsByEventIdAndConsumerGroup(eventId, consumerGroup)).thenReturn(true);

        boolean isNew = idempotencyService.recordProcessingIfNew(eventId, consumerGroup, eventType);

        assertFalse(isNew);
        verify(processedEventRepository, never()).save(any(ProcessedEvent.class));
    }
}
