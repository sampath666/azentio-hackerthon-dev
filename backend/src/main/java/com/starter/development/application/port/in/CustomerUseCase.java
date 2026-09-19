package com.starter.development.application.port.in;

import com.starter.development.domain.model.Customer;

import java.util.List;

public interface CustomerUseCase {

    Customer createCustomer(String name, String kycId, String riskRating, String country);

    List<Customer> getCustomers();

    Customer getCustomer(Long id);

    Customer getCustomerByKycId(String kycId);
}
