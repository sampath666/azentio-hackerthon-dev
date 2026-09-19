package com.starter.development.application.port.out;

import com.starter.development.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepositoryPort {

    Account save(Account account, Long customerId);

    List<Account> findAll();

    List<Account> findByCustomerId(Long customerId);

    Optional<Account> findById(Long id);

    Optional<Account> findByAccountNumber(String accountNumber);
}
