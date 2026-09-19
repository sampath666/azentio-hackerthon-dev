package com.starter.development.domain.model;

// Transaction
// - id
// - account id
// - amount
// - currency
// - normalized amount in INR
// - transaction type: DEPOSIT, WITHDRAWAL, TRANSFER
// - counterparty
// - counterparty country
// - channel
// - transaction timestamp
// - created date


public class Transaction {
    private Long id;
    private String account;
    private Double amount;
    private String currency;
    private Double normalizedAmountInr;
    private String timestamp;
    private String type;
    private String counterparty;
    private String jurisdiction;
    private String channel;
    private String createdDate;


    public Transaction(Long id, String account, Double amount, String currency, String timestamp, String type, String counterparty, String jurisdiction, String channel, String createdDate) {
        this(id, account, amount, currency, null, timestamp, type, counterparty, jurisdiction, channel, createdDate);
    }

    public Transaction(Long id, String account, Double amount, String currency, Double normalizedAmountInr,
                       String timestamp, String type, String counterparty, String jurisdiction,
                       String channel, String createdDate) {
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

    public Transaction(String account, Double amount, String currency, String timestamp, String type, String counterparty, String jurisdiction) {
        this(null, account, amount, currency, timestamp, type, counterparty, jurisdiction, null, null);
    }

    public Long getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public Double getAmount() {
        return amount;
    }

    public Double getNormalizedAmountInr() {
        return normalizedAmountInr;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public String getChannel() {
        return channel;
    }

    public String getCreatedDate() {
        return createdDate;
    }
    
}