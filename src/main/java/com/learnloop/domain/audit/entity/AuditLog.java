package com.learnloop.domain.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "action", length = 100, nullable = false)
    private String action;

    @Column(name = "resource_type", length = 50, nullable = false)
    private String resourceType;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(String userId, String action, String resourceType, String resourceId, String details, String ipAddress) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
