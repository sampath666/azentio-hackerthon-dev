package com.starter.development.application.port.in;

import com.starter.development.domain.model.AuditEvent;

import java.util.List;

public interface AuditEventUseCase {

    List<AuditEvent> getEventsForEntity(Long entityId);
}
