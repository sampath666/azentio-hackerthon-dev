package com.starter.development.adapter.out.persistence;

import com.starter.development.domain.model.Case;
import jakarta.persistence.*;

@Entity
@Table(name = "cases")
public class CaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlertJpaEntity alert;

    private Long analystId;
    private String disposition;

    @Column(length = 2000)
    private String notes;

    private String createdAt;
    private String updatedAt;

    protected CaseJpaEntity() {
    }

    public CaseJpaEntity(Long id, String title, String description, String status, AlertJpaEntity alert,
                         Long analystId, String disposition, String notes, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.alert = alert;
        this.analystId = analystId;
        this.disposition = disposition;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Case toDomain() {
        return new Case(id, title, description, status, alert.getId(), analystId, disposition,
                notes, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public AlertJpaEntity getAlert() { return alert; }
}