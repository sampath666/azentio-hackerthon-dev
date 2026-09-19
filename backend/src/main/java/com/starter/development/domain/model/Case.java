package com.starter.development.domain.model;

public class Case {
    private Long id;
    private String title;
    private String description;
    private String status;
    private Long alertId;
    private Long analystId;
    private String disposition;
    private String notes;
    private String createdAt;
    private String updatedAt;

    public Case(Long id, String title, String description, String status, Long alertId, Long analystId, String disposition, String notes, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.alertId = alertId;
        this.analystId = analystId;
        this.disposition = disposition;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Case(String title, String description, String status, Long alertId, Long analystId, String disposition, String notes) {
        this(null, title, description, status, alertId, analystId, disposition, notes, null, null);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public Long getAlertId() {
        return alertId;
    }

    public Long getAnalystId() {
        return analystId;
    }

    public String getDisposition() {
        return disposition;
    }

    public String getNotes() {
        return notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}