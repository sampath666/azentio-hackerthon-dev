package com.starter.development.application.service;

import com.starter.development.application.port.in.AuditEventUseCase;
import com.starter.development.application.port.out.AuditEventRepositoryPort;
import com.starter.development.domain.model.AuditEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditEventService implements AuditEventUseCase {

    private final AuditEventRepositoryPort repository;

    public AuditEventService(AuditEventRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<AuditEvent> getEventsForEntity(Long entityId) {
        return repository.findByEntityId(entityId);
    }
}
