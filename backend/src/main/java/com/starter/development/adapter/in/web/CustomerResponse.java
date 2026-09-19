package com.starter.development.adapter.in.web;

import com.starter.development.domain.model.Customer;

public record CustomerResponse(Long id, String name, String kycId, String riskRating,
                               String country, String createdDate) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName(), customer.getKycId(),
                customer.getRiskRating(), customer.getCountry(), customer.getCreatedDate());
    }

    public static CustomerResponse masked(Customer customer) {
        return new CustomerResponse(customer.getId(), maskName(customer.getName()),
                maskIdentifier(customer.getKycId()), customer.getRiskRating(),
                customer.getCountry(), customer.getCreatedDate());
    }

    private static String maskName(String name) {
        if (name == null || name.isBlank()) return name;
        return name.charAt(0) + "***";
    }

    private static String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() <= 4) return "****";
        return "****" + identifier.substring(identifier.length() - 4);
    }
}
