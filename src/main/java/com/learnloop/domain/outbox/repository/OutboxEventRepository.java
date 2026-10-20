package com.learnloop.domain.outbox.repository;

import com.learnloop.domain.outbox.entity.OutboxEvent;
import com.learnloop.domain.outbox.entity.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OutboxEvent> findPendingEvents(@Param("status") OutboxStatus status,
                                        @Param("maxRetries") int maxRetries,
                                        Pageable pageable);

    long countByStatus(OutboxStatus status);
}
