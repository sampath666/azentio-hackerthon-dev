package com.starter.development.domain.model;

public class Account {
    private Long id;
    private String customer;
    private String accountNumber;
    private String type;
    private String currency;
    private String openingDate;
    private String status;


    public Account(Long id, String customer, String accountNumber, String type, String currency, String openingDate, String status) {
        this.id = id;
        this.customer = customer;
        this.accountNumber = accountNumber;
        this.type = type;
        this.currency = currency;
        this.openingDate = openingDate;
        this.status = status;
    }

    public Account(String customer, String accountNumber, String type, String currency, String openingDate, String status) {
        this(null, customer, accountNumber, type, currency, openingDate, status);
    }

    public Long getId() {
        return id;
    }

    public String getCustomer() {
        return customer;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public String getOpeningDate() {
        return openingDate;
    }

    public String getStatus() {
        return status;
    }
}