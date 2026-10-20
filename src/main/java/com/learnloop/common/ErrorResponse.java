package com.learnloop.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;
    private String errorCode;
    private String message;
    private Map<String, String> details;
    private String correlationId;
    private String timestamp;

    public ErrorResponse() {
        this.success = false;
        this.timestamp = Instant.now().toString();
    }

    public ErrorResponse(String errorCode, String message, String correlationId) {
        this.success = false;
        this.errorCode = errorCode;
        this.message = message;
        this.correlationId = correlationId;
        this.timestamp = Instant.now().toString();
    }

    public ErrorResponse(String errorCode, String message, Map<String, String> details, String correlationId) {
        this.success = false;
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.correlationId = correlationId;
        this.timestamp = Instant.now().toString();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
