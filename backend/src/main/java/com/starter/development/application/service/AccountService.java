package com.starter.development.application.service;

import com.starter.development.application.port.in.AccountUseCase;
import com.starter.development.application.port.out.AccountRepositoryPort;
import com.starter.development.domain.model.Account;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService implements AccountUseCase {

    private final AccountRepositoryPort repository;

    public AccountService(AccountRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Account createAccount(Long customerId, String accountNumber, String type,
                                 String currency, String openingDate, String status) {
        if (customerId == null || accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Customer ID and account number are required");
        }
        if (repository.findByAccountNumber(accountNumber).isPresent()) {
            throw new IllegalArgumentException("Account number already exists");
        }
        return repository.save(new Account(customerId.toString(), accountNumber, type, currency,
                openingDate, status), customerId);
    }

    @Override
    public List<Account> getAccounts() { return repository.findAll(); }

    @Override
    public List<Account> getAccountsByCustomer(Long customerId) { return repository.findByCustomerId(customerId); }

    @Override
    public Account getAccount(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Account not found: " + id));
    }
}
