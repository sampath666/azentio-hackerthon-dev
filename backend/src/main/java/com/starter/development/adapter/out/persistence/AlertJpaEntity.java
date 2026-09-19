package com.starter.development.adapter.out.persistence;

import com.starter.development.domain.model.Alert;
import jakarta.persistence.*;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alerts_status_score", columnList = "status, score")
})
public class AlertJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerJpaEntity customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private AccountJpaEntity account;

    @Column(nullable = false)
    private String ruleType;

    @Column(nullable = false)
    private Double score;

    @Column(length = 2000)
    private String explanation;

    @Column(length = 2000)
    private String evidenceTransactionIds;

    @Column(nullable = false)
    private String status;

    private String createdDate;
    private String updatedDate;

    protected AlertJpaEntity() {
    }

    public AlertJpaEntity(Long id, CustomerJpaEntity customer, AccountJpaEntity account, String ruleType,
                          Double score, String explanation, String evidenceTransactionIds, String status,
                          String createdDate, String updatedDate) {
        this.id = id;
        this.customer = customer;
        this.account = account;
        this.ruleType = ruleType;
        this.score = score;
        this.explanation = explanation;
        this.evidenceTransactionIds = evidenceTransactionIds;
        this.status = status;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public Alert toDomain() {
        return new Alert(id, customer == null ? null : customer.getId(),
                account == null ? null : account.getId(), ruleType, score, explanation,
                evidenceTransactionIds, status, createdDate, updatedDate);
    }

    public Long getId() { return id; }
    public String getRuleType() { return ruleType; }
    public String getStatus() { return status; }
    public Double getScore() { return score; }
}