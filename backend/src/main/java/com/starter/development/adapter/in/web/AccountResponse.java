package com.starter.development.adapter.in.web;

import com.starter.development.domain.model.Account;

public record AccountResponse(Long id, String customer, String accountNumber, String type,
                              String currency, String openingDate, String status) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getCustomer(), account.getAccountNumber(),
                account.getType(), account.getCurrency(), account.getOpeningDate(), account.getStatus());
    }
}
