package com.starter.development.adapter.out.persistence;

import com.starter.development.application.port.out.AuditEventRepositoryPort;
import com.starter.development.domain.model.AuditEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditEventPersistenceAdapter implements AuditEventRepositoryPort {

    private final SpringDataAuditEventRepository repository;

    public AuditEventPersistenceAdapter(SpringDataAuditEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        AuditEventJpaEntity entity = new AuditEventJpaEntity(event.getId(), event.getEntityId(),
                event.getActor(), event.getPreviousState(), event.getNewState(), event.getAction(),
                event.getTimestamp(), event.getReason());
        return repository.save(entity).toDomain();
    }

    @Override
    public List<AuditEvent> findByEntityId(Long entityId) {
        return repository.findByEntityIdOrderByIdAsc(entityId).stream()
                .map(AuditEventJpaEntity::toDomain).toList();
    }
}
