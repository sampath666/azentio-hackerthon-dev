package com.starter.development.adapter.in.web;

import com.starter.development.domain.model.AuditEvent;

public record AuditEventResponse(Long id, Long entityId, String actor, String previousState,
                                 String newState, String action, String timestamp, String reason) {

    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getEntityId(), event.getActor(),
                event.getPreviousState(), event.getNewState(), event.getAction(),
                event.getTimestamp(), event.getReason());
    }
}
