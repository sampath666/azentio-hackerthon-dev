package com.starter.development.adapter.in.web;

import com.starter.development.domain.model.Case;

public record CaseResponse(Long id, String title, String description, String status, Long alertId,
                           Long analystId, String disposition, String notes, String createdAt,
                           String updatedAt) {

    public static CaseResponse from(Case caseFile) {
        return new CaseResponse(caseFile.getId(), caseFile.getTitle(), caseFile.getDescription(),
                caseFile.getStatus(), caseFile.getAlertId(), caseFile.getAnalystId(),
                caseFile.getDisposition(), caseFile.getNotes(), caseFile.getCreatedAt(),
                caseFile.getUpdatedAt());
    }
}
