package com.starter.development.domain.model;

public record TransactionCommand(
        Long accountId,
        Double amount,
        String currency,
        String timestamp,
        String type,
        String counterparty,
        String jurisdiction,
        String channel
) {
}