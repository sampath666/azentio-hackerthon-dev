package com.starter.development.adapter.in.web;

import com.starter.development.domain.model.Transaction;

public record TransactionResponse(Long id, String account, Double amount, String currency,
                                  Double normalizedAmountInr,
                                  String timestamp, String type, String counterparty,
                                  String jurisdiction, String channel, String createdDate) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getAccount(), transaction.getAmount(),
            transaction.getCurrency(), transaction.getNormalizedAmountInr(), transaction.getTimestamp(), transaction.getType(),
                transaction.getCounterparty(), transaction.getJurisdiction(), transaction.getChannel(),
                transaction.getCreatedDate());
    }
}
