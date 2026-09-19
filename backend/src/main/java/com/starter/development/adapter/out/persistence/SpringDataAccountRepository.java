package com.starter.development.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, Long> {

    Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);

    java.util.List<AccountJpaEntity> findByCustomer_Id(Long customerId);
}
