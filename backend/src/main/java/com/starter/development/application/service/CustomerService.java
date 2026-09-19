package com.starter.development.application.service;

import com.starter.development.application.port.in.CustomerUseCase;
import com.starter.development.application.port.out.CustomerRepositoryPort;
import com.starter.development.domain.model.Customer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CustomerService implements CustomerUseCase {

    private final CustomerRepositoryPort repository;

    public CustomerService(CustomerRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Customer createCustomer(String name, String kycId, String riskRating, String country) {
        if (name == null || name.isBlank() || kycId == null || kycId.isBlank()) {
            throw new IllegalArgumentException("Customer name and KYC ID are required");
        }
        if (repository.findByKycId(kycId).isPresent()) {
            throw new IllegalArgumentException("KYC ID already exists");
        }
        return repository.save(new Customer(name, kycId, riskRating, country, Instant.now().toString()));
    }

    @Override
    public List<Customer> getCustomers() { return repository.findAll(); }

    @Override
    public Customer getCustomer(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Customer not found: " + id));
    }

    @Override
    public Customer getCustomerByKycId(String kycId) {
        return repository.findByKycId(kycId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + kycId));
    }
}
