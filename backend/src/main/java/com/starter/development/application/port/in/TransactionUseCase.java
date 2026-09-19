package com.starter.development.application.port.in;

import com.starter.development.domain.model.Transaction;
import com.starter.development.domain.model.TransactionIngestionResult;
import com.starter.development.domain.model.BulkTransactionResult;
import com.starter.development.domain.model.TransactionCommand;

import java.util.List;

public interface TransactionUseCase {

    TransactionIngestionResult createTransaction(Long accountId, Double amount, String currency,
                                                 String timestamp, String type, String counterparty,
                                                 String jurisdiction, String channel);

    BulkTransactionResult createBulkTransactions(List<TransactionCommand> transactions);

    List<Transaction> getTransactions();

    List<Transaction> getTransactionsByAccount(Long accountId, String from, String to);

    Transaction getTransaction(Long id);
}
