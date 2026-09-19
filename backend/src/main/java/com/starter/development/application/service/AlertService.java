package com.starter.development.application.service;

import com.starter.development.application.port.in.AlertUseCase;
import com.starter.development.application.port.out.AlertRepositoryPort;
import com.starter.development.application.port.out.AuditEventRepositoryPort;
import com.starter.development.domain.model.Alert;
import com.starter.development.domain.model.AuditEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AlertService implements AlertUseCase {

    private final AlertRepositoryPort repository;
    private final AuditEventRepositoryPort auditRepository;

    public AlertService(AlertRepositoryPort repository, AuditEventRepositoryPort auditRepository) {
        this.repository = repository;
        this.auditRepository = auditRepository;
    }

    @Override
    public List<Alert> getAlerts() { return repository.findAll(); }

    @Override
    public List<Alert> getAlertsByStatus(String status) { return repository.findByStatus(status); }

    @Override
    public Alert getAlert(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Alert not found: " + id));
    }

    @Override
    public Alert updateStatus(Long id, String status, String actor, String reason) {
        Alert current = getAlert(id);
        if (status == null || !List.of("OPEN", "IN_REVIEW", "CLEARED", "ESCALATED").contains(status)) {
            throw new IllegalArgumentException("Unsupported alert status: " + status);
        }
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("Analyst identity is required");
        }
        if ("CLEARED".equals(status) && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("Disposition reason is required when clearing an alert");
        }
        Alert updated = new Alert(current.getId(), current.getCustomerId(), current.getAccountId(),
                current.getRuleType(), current.getScore(), current.getExplanation(),
                current.getEvidenceTransactionIds(), status, current.getCreatedDate(), Instant.now().toString());
        Alert saved = repository.save(updated);
        auditRepository.save(new AuditEvent(id, actor, current.getStatus(), status,
                "ALERT_STATUS_CHANGED", Instant.now().toString(), reason));
        return saved;
    }
}
