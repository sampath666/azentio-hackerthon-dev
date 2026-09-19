package com.starter.development.adapter.out.persistence;

import com.starter.development.domain.model.Customer;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
public class CustomerJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String kycId;

	@Column(nullable = false)
	private String riskRating;

	@Column(nullable = false)
	private String country;

	private String createdDate;

	@OneToMany(mappedBy = "customer")
	private List<AccountJpaEntity> accounts = new ArrayList<>();

	protected CustomerJpaEntity() {
	}

	public CustomerJpaEntity(Long id, String name, String kycId, String riskRating, String country, String createdDate) {
		this.id = id;
		this.name = name;
		this.kycId = kycId;
		this.riskRating = riskRating;
		this.country = country;
		this.createdDate = createdDate;
	}

	public static CustomerJpaEntity fromDomain(Customer customer) {
		return new CustomerJpaEntity(customer.getId(), customer.getName(), customer.getKycId(),
				customer.getRiskRating(), customer.getCountry(), customer.getCreatedDate());
	}

	public Customer toDomain() {
		return new Customer(id, name, kycId, riskRating, country, createdDate);
	}

	public Long getId() { return id; }
	public String getKycId() { return kycId; }
}