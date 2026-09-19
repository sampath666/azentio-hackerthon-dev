package com.starter.development.domain.model;

public class Alert {
    private Long id;
    private Long customerId;
    private Long accountId;
    private String ruleType;
    private Double score;
    private String explanation;
    private String evidenceTransactionIds;
    private String status;
    private String createdDate;
    private String updatedDate;

    public Alert(Long id, String ruleType, Double score, String explanation, String evidenceTransactionIds, String status, String createdDate, String updatedDate) {
        this(id, null, null, ruleType, score, explanation, evidenceTransactionIds, status, createdDate, updatedDate);
    }

    public Alert(Long id, Long customerId, Long accountId, String ruleType, Double score, String explanation, String evidenceTransactionIds, String status, String createdDate, String updatedDate) {
        this.id = id;
        this.customerId = customerId;
        this.accountId = accountId;
        this.ruleType = ruleType;
        this.score = score;
        this.explanation = explanation;
        this.evidenceTransactionIds = evidenceTransactionIds;
        this.status = status;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public Alert(String ruleType, Double score, String explanation, String evidenceTransactionIds, String status) {
        this(null, null, null, ruleType, score, explanation, evidenceTransactionIds, status, null, null);
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getRuleType() {
        return ruleType;
    }

    public Double getScore() {
        return score;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getEvidenceTransactionIds() {
        return evidenceTransactionIds;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }
}