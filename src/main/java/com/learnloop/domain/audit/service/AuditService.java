package com.learnloop.domain.audit.service;

import com.learnloop.domain.audit.entity.AuditLog;
import com.learnloop.domain.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(String userId, String action, String resourceType, String resourceId, String details, String ipAddress) {
        AuditLog auditLog = new AuditLog(userId, action, resourceType, resourceId, details, ipAddress);
        auditLogRepository.save(auditLog);
        log.debug("Audit logged: user={}, action={}, resource={}:{}", userId, action, resourceType, resourceId);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getRecentAuditLogs(Pageable pageable) {
        return auditLogRepository.findByOrderByCreatedAtDesc(pageable);
    }
}
