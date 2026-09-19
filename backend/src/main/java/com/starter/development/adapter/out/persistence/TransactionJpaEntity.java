package com.starter.development.adapter.out.persistence;

import com.starter.development.domain.model.Transaction;
import jakarta.persistence.*;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transactions_account", columnList = "account_id"),
        @Index(name = "idx_transactions_timestamp", columnList = "transaction_timestamp")
})
public class TransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountJpaEntity account;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Double normalizedAmountInr;

    @Column(name = "transaction_timestamp", nullable = false)
    private String timestamp;

    @Column(nullable = false)
    private String type;

    private String counterparty;
    private String jurisdiction;
    private String channel;
    private String createdDate;

    protected TransactionJpaEntity() {
    }

    public TransactionJpaEntity(Long id, AccountJpaEntity account, Double amount, String currency,
                                Double normalizedAmountInr, String timestamp, String type,
                                String counterparty, String jurisdiction, String channel, String createdDate) {
        this.id = id;
        this.account = account;
        this.amount = amount;
        this.currency = currency;
        this.normalizedAmountInr = normalizedAmountInr;
        this.timestamp = timestamp;
        this.type = type;
        this.counterparty = counterparty;
        this.jurisdiction = jurisdiction;
        this.channel = channel;
        this.createdDate = createdDate;
    }

    public Transaction toDomain() {
        return new Transaction(id, account.getAccountNumber(), amount, currency, normalizedAmountInr, timestamp, type,
                counterparty, jurisdiction, channel, createdDate);
    }

    public Long getId() { return id; }
    public AccountJpaEntity getAccount() { return account; }
    public Double getNormalizedAmountInr() { return normalizedAmountInr; }
    public String getTimestamp() { return timestamp; }
    public String getType() { return type; }
}