package com.starter.development.adapter.in.web;

import com.starter.development.application.port.in.CaseUseCase;
import com.starter.development.domain.model.Case;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseUseCase caseUseCase;

    public CaseController(CaseUseCase caseUseCase) {
        this.caseUseCase = caseUseCase;
    }

    @PostMapping
    public CaseResponse createCase(@RequestBody CreateCaseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Case request body is required");
        }
        Case caseFile = caseUseCase.createCase(request.alertId(), request.title(),
                request.description(), request.analystId());
        return CaseResponse.from(caseFile);
    }

    @GetMapping
    public List<CaseResponse> getCases() {
        return caseUseCase.getCases().stream().map(CaseResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CaseResponse getCase(@PathVariable Long id) {
        return CaseResponse.from(caseUseCase.getCase(id));
    }

    @PatchMapping("/{id}/assign")
    public CaseResponse assignCase(@PathVariable Long id,
                                   @RequestBody AssignCaseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Case assignment request body is required");
        }
        return CaseResponse.from(caseUseCase.assignCase(id, request.analystId()));
    }

    @PatchMapping("/{id}/disposition")
    public CaseResponse updateDisposition(@PathVariable Long id,
                                          @RequestBody UpdateDispositionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Case disposition request body is required");
        }
        return CaseResponse.from(caseUseCase.updateDisposition(id, request.status(),
                request.disposition(), request.notes(), request.actor()));
    }
}

record CreateCaseRequest(Long alertId, String title, String description, Long analystId) {
}

record AssignCaseRequest(Long analystId) {
}

record UpdateDispositionRequest(String status, String disposition, String notes, String actor) {
}
