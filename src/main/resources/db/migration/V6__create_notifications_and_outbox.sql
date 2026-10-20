-- V6__create_notifications_and_outbox.sql
-- Create notifications, transactional outbox, idempotent processed_events, and audit_logs tables (PostgreSQL)

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50) NULL,
    reference_id VARCHAR(100) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user ON notifications (user_id);
CREATE INDEX idx_notifications_unread ON notifications (user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications (created_at DESC);

CREATE TABLE IF NOT EXISTS outbox_events (
    id VARCHAR(36) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_status ON outbox_events (status, created_at ASC);
CREATE INDEX idx_outbox_aggregate ON outbox_events (aggregate_id);

CREATE TABLE IF NOT EXISTS processed_events (
    id VARCHAR(36) NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    consumer_group VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_processed_events PRIMARY KEY (id),
    CONSTRAINT uq_processed_events UNIQUE (event_id, consumer_group)
);

CREATE INDEX idx_processed_events_lookup ON processed_events (event_id, consumer_group);

CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100) NULL,
    details TEXT NULL,
    ip_address VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);

CREATE INDEX idx_audit_logs_user ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at DESC);
