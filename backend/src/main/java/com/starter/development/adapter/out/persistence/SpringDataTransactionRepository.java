package com.starter.development.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, Long> {

    List<TransactionJpaEntity> findByAccount_IdAndTimestampBetweenOrderByTimestampAsc(
            Long accountId, String from, String to);

    List<TransactionJpaEntity> findByAccount_IdOrderByTimestampAsc(Long accountId);
}
