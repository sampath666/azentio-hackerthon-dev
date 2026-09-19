package com.starter.development.application.port.out;

import com.starter.development.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction, Long accountId, Double normalizedAmountInr);

    List<Transaction> findAll();

    Optional<Transaction> findById(Long id);

    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByAccountIdAndTimestampBetween(Long accountId, String from, String to);
}
