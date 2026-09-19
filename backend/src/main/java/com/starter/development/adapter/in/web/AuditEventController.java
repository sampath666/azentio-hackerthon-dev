package com.starter.development.adapter.in.web;

import com.starter.development.application.port.in.AuditEventUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-events")
public class AuditEventController {

    private final AuditEventUseCase auditEventUseCase;

    public AuditEventController(AuditEventUseCase auditEventUseCase) {
        this.auditEventUseCase = auditEventUseCase;
    }

    @GetMapping("/{entityId}")
    public List<AuditEventResponse> getEvents(@PathVariable Long entityId) {
        return auditEventUseCase.getEventsForEntity(entityId).stream()
                .map(AuditEventResponse::from)
                .toList();
    }
}
