package com.learnloop.domain.kafka.event;

import java.time.Instant;
import java.util.UUID;

public abstract class BaseEvent {

    private String eventId;
    private String eventType;
    private String schemaVersion;
    private String aggregateId;
    private String timestamp;

    public BaseEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.schemaVersion = "1.0";
        this.timestamp = Instant.now().toString();
    }

    public BaseEvent(String eventType, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.schemaVersion = "1.0";
        this.aggregateId = aggregateId;
        this.timestamp = Instant.now().toString();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
