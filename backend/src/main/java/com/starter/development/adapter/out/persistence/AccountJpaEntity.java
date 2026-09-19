package com.starter.development.adapter.out.persistence;

import com.starter.development.domain.model.Account;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts", indexes = @Index(name = "idx_accounts_customer", columnList = "customer_id"))
public class AccountJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    private String type;
    private String currency;
    private String openingDate;
    private String status;

    @OneToMany(mappedBy = "account")
    private List<TransactionJpaEntity> transactions = new ArrayList<>();

    protected AccountJpaEntity() {
    }

    public AccountJpaEntity(Long id, CustomerJpaEntity customer, String accountNumber, String type,
                            String currency, String openingDate, String status) {
        this.id = id;
        this.customer = customer;
        this.accountNumber = accountNumber;
        this.type = type;
        this.currency = currency;
        this.openingDate = openingDate;
        this.status = status;
    }

    public Account toDomain() {
        return new Account(id, customer.getId().toString(), accountNumber, type, currency, openingDate, status);
    }

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public CustomerJpaEntity getCustomer() { return customer; }
}