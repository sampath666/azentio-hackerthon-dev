package com.starter.development.domain.model;

public class Customer {
    private Long id;
    private String name;
    private String kycId;
    private String riskRating;
    private String country;
    private String createdDate;

    public Customer(Long id, String name, String kycId, String riskRating, String country, String createdDate) {
        this.id = id;
        this.name = name;
        this.kycId = kycId;
        this.riskRating = riskRating;
        this.country = country;
        this.createdDate = createdDate;
    }

    public Customer(String name, String kycId, String riskRating, String country, String createdDate) {
        this(null, name, kycId, riskRating, country, createdDate);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getKycId() {
        return kycId;
    }

    public String getRiskRating() {
        return riskRating;
    }

    public String getCountry() {
        return country;
    }

    public String getCreatedDate() {
        return createdDate;
    }
}