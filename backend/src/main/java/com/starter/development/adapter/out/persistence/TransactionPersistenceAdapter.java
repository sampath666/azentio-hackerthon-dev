package com.starter.development.adapter.out.persistence;

import com.starter.development.application.port.out.TransactionRepositoryPort;
import com.starter.development.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository repository;
    private final SpringDataAccountRepository accountRepository;

    public TransactionPersistenceAdapter(SpringDataTransactionRepository repository,
                                         SpringDataAccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Transaction save(Transaction transaction, Long accountId, Double normalizedAmountInr) {
        AccountJpaEntity account = accountRepository.getReferenceById(accountId);
        TransactionJpaEntity entity = new TransactionJpaEntity(transaction.getId(), account,
                transaction.getAmount(), transaction.getCurrency(), normalizedAmountInr,
                transaction.getTimestamp(), transaction.getType(), transaction.getCounterparty(),
                transaction.getJurisdiction(), transaction.getChannel(), transaction.getCreatedDate());
        return repository.save(entity).toDomain();
    }

    @Override
    public List<Transaction> findAll() {
        return repository.findAll().stream().map(TransactionJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return repository.findById(id).map(TransactionJpaEntity::toDomain);
    }

    @Override
    public List<Transaction> findByAccountId(Long accountId) {
        return repository.findByAccount_IdOrderByTimestampAsc(accountId).stream()
                .map(TransactionJpaEntity::toDomain).toList();
    }

    @Override
    public List<Transaction> findByAccountIdAndTimestampBetween(Long accountId, String from, String to) {
        return repository.findByAccount_IdAndTimestampBetweenOrderByTimestampAsc(accountId, from, to)
                .stream().map(TransactionJpaEntity::toDomain).toList();
    }
}
