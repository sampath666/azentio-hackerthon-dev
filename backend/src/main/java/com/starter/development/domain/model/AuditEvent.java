package com.starter.development.domain.model;

public class AuditEvent {
    private Long id;
    private Long entityId;
    private String actor;
    private String previousState;
    private String newState;
    private String action;
    private String timestamp;
    private String reason;

    public AuditEvent(Long id, Long entityId, String actor, String previousState, String newState, String action, String timestamp, String reason) {
        this.id = id;
        this.entityId = entityId;
        this.actor = actor;
        this.previousState = previousState;
        this.newState = newState;
        this.action = action;
        this.timestamp = timestamp;
        this.reason = reason;
    }

    public AuditEvent(Long entityId, String actor, String previousState, String newState, String action, String timestamp, String reason) {
        this(null, entityId, actor, previousState, newState, action, timestamp, reason);
    }

    public Long getId() {
        return id;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getActor() {
        return actor;
    }

    public String getPreviousState() {
        return previousState;
    }

    public String getNewState() {
        return newState;
    }

    public String getAction() {
        return action;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getReason() {
        return reason;
    }
}