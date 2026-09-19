package com.starter.development.application.service;

import com.starter.development.application.port.in.CaseUseCase;
import com.starter.development.application.port.out.CaseRepositoryPort;
import com.starter.development.application.port.out.AuditEventRepositoryPort;
import com.starter.development.domain.model.Case;
import com.starter.development.domain.model.AuditEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CaseService implements CaseUseCase {

    private final CaseRepositoryPort repository;
    private final AuditEventRepositoryPort auditRepository;

    public CaseService(CaseRepositoryPort repository, AuditEventRepositoryPort auditRepository) {
        this.repository = repository;
        this.auditRepository = auditRepository;
    }

    @Override
    public Case createCase(Long alertId, String title, String description, Long analystId) {
        if (alertId == null || title == null || title.isBlank()) {
            throw new IllegalArgumentException("Alert ID and case title are required");
        }
        return repository.save(new Case(title, description, "OPEN", alertId, analystId,
                null, null));
    }

    @Override
    public List<Case> getCases() { return repository.findAll(); }

    @Override
    public Case getCase(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Case not found: " + id));
    }

    @Override
    public Case assignCase(Long id, Long analystId) {
        if (analystId == null) throw new IllegalArgumentException("Analyst ID is required");
        Case current = getCase(id);
        Case saved = repository.save(new Case(current.getId(), current.getTitle(), current.getDescription(),
                current.getStatus(), current.getAlertId(), analystId, current.getDisposition(),
                current.getNotes(), current.getCreatedAt(), Instant.now().toString()));
        auditRepository.save(new AuditEvent(id, String.valueOf(analystId), current.getStatus(), current.getStatus(),
            "CASE_ASSIGNED", Instant.now().toString(), "Case assigned"));
        return saved;
    }

    @Override
    public Case updateDisposition(Long id, String status, String disposition, String notes, String actor) {
        Case current = getCase(id);
        if (status == null || !List.of("OPEN", "IN_REVIEW", "CLEARED", "ESCALATED").contains(status)) {
            throw new IllegalArgumentException("Unsupported case status: " + status);
        }
        if (actor == null || actor.isBlank()) throw new IllegalArgumentException("Actor identity is required");
        if ("CLEARED".equals(status) && (disposition == null || disposition.isBlank())) {
            throw new IllegalArgumentException("Disposition reason is required when closing a case");
        }
        Case saved = repository.save(new Case(current.getId(), current.getTitle(), current.getDescription(),
                status, current.getAlertId(), current.getAnalystId(), disposition, notes,
                current.getCreatedAt(), Instant.now().toString()));
        auditRepository.save(new AuditEvent(id, actor, current.getStatus(), status,
                "CASE_DISPOSITION_CHANGED", Instant.now().toString(), disposition));
        return saved;
    }
}
