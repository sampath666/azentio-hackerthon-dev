package com.starter.development.adapter.out.persistence;

import com.starter.development.domain.model.AuditEvent;
import jakarta.persistence.*;

@Entity
@Table(name = "audit_events", indexes = @Index(name = "idx_audit_entity", columnList = "entity_id"))
public class AuditEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String actor;

    private String previousState;
    private String newState;
    private String action;
    private String timestamp;

    @Column(length = 2000)
    private String reason;

    protected AuditEventJpaEntity() {
    }

    public AuditEventJpaEntity(Long id, Long entityId, String actor, String previousState, String newState,
                               String action, String timestamp, String reason) {
        this.id = id;
        this.entityId = entityId;
        this.actor = actor;
        this.previousState = previousState;
        this.newState = newState;
        this.action = action;
        this.timestamp = timestamp;
        this.reason = reason;
    }

    public AuditEvent toDomain() {
        return new AuditEvent(id, entityId, actor, previousState, newState, action, timestamp, reason);
    }

    public Long getId() { return id; }
    public Long getEntityId() { return entityId; }
}