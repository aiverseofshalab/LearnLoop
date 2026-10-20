package com.learnloop.domain.audit.repository;

import com.learnloop.domain.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    Page<AuditLog> findByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
