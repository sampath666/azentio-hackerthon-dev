package com.starter.development.application.port.out;

import com.starter.development.domain.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryPort {

    Customer save(Customer customer);

    List<Customer> findAll();

    Optional<Customer> findById(Long id);

    Optional<Customer> findByKycId(String kycId);
}
