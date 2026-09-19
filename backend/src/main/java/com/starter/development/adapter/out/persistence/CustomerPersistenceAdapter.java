package com.starter.development.adapter.out.persistence;

import com.starter.development.application.port.out.CustomerRepositoryPort;
import com.starter.development.domain.model.Customer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final SpringDataCustomerRepository repository;

    public CustomerPersistenceAdapter(SpringDataCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer save(Customer customer) {
        return repository.save(CustomerJpaEntity.fromDomain(customer)).toDomain();
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAll().stream().map(CustomerJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return repository.findById(id).map(CustomerJpaEntity::toDomain);
    }

    @Override
    public Optional<Customer> findByKycId(String kycId) {
        return repository.findByKycId(kycId).map(CustomerJpaEntity::toDomain);
    }
}
