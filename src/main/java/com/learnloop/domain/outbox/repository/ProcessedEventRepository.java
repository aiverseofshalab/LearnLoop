package com.learnloop.domain.outbox.repository;

import com.learnloop.domain.outbox.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    boolean existsByEventIdAndConsumerGroup(String eventId, String consumerGroup);

    Optional<ProcessedEvent> findByEventIdAndConsumerGroup(String eventId, String consumerGroup);
}
