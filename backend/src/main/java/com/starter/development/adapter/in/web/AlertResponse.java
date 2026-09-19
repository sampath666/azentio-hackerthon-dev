package com.starter.development.adapter.in.web;

import com.starter.development.domain.model.Alert;

public record AlertResponse(Long id, Long customerId, Long accountId, String ruleType, Double score,
                            String explanation, String evidenceTransactionIds, String status,
                            String createdDate, String updatedDate) {

    public static AlertResponse from(Alert alert) {
        return new AlertResponse(alert.getId(), alert.getCustomerId(), alert.getAccountId(),
                alert.getRuleType(), alert.getScore(), alert.getExplanation(),
                alert.getEvidenceTransactionIds(), alert.getStatus(), alert.getCreatedDate(),
                alert.getUpdatedDate());
    }
}
