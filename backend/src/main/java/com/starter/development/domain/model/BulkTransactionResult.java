package com.starter.development.domain.model;

import java.util.List;

public record BulkTransactionResult(
        int acceptedCount,
        int rejectedCount,
        int alertedCount,
        List<TransactionIngestionResult> acceptedTransactions,
        List<RejectedTransaction> rejectedTransactions
) {
}