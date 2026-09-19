package com.starter.development.adapter.in.web;

import com.starter.development.domain.model.BulkTransactionResult;
import com.starter.development.domain.model.RejectedTransaction;
import com.starter.development.domain.model.TransactionIngestionResult;

import java.util.List;

public record BulkTransactionResponse(
        int acceptedCount,
        int rejectedCount,
        int alertedCount,
        List<TransactionIngestionResponse> acceptedTransactions,
        List<RejectedTransactionResponse> rejectedTransactions
) {

    public static BulkTransactionResponse from(BulkTransactionResult result) {
        return new BulkTransactionResponse(
                result.acceptedCount(),
                result.rejectedCount(),
                result.alertedCount(),
                result.acceptedTransactions().stream()
                        .map(TransactionIngestionResponse::from)
                        .toList(),
                result.rejectedTransactions().stream()
                        .map(RejectedTransactionResponse::from)
                        .toList()
        );
    }
}

record RejectedTransactionResponse(int rowNumber, String reason) {

    static RejectedTransactionResponse from(RejectedTransaction rejectedTransaction) {
        return new RejectedTransactionResponse(rejectedTransaction.rowNumber(), rejectedTransaction.reason());
    }
}
