package com.starter.development.application.port.out;

import com.starter.development.domain.model.AuditEvent;

import java.util.List;

public interface AuditEventRepositoryPort {

    AuditEvent save(AuditEvent event);

    List<AuditEvent> findByEntityId(Long entityId);
}
