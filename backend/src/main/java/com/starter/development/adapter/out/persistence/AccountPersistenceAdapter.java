package com.starter.development.adapter.out.persistence;

import com.starter.development.application.port.out.AccountRepositoryPort;
import com.starter.development.domain.model.Account;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository repository;
    private final SpringDataCustomerRepository customerRepository;

    public AccountPersistenceAdapter(SpringDataAccountRepository repository,
                                     SpringDataCustomerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Account save(Account account, Long customerId) {
        CustomerJpaEntity customer = customerRepository.getReferenceById(customerId);
        AccountJpaEntity entity = new AccountJpaEntity(account.getId(), customer,
                account.getAccountNumber(), account.getType(), account.getCurrency(),
                account.getOpeningDate(), account.getStatus());
        return repository.save(entity).toDomain();
    }

    @Override
    public List<Account> findAll() {
        return repository.findAll().stream().map(AccountJpaEntity::toDomain).toList();
    }

    @Override
    public List<Account> findByCustomerId(Long customerId) {
        return repository.findByCustomer_Id(customerId).stream()
                .map(AccountJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Account> findById(Long id) {
        return repository.findById(id).map(AccountJpaEntity::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).map(AccountJpaEntity::toDomain);
    }
}
