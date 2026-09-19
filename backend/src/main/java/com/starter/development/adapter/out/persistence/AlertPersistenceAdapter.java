package com.starter.development.adapter.out.persistence;

import com.starter.development.application.port.out.AlertRepositoryPort;
import com.starter.development.domain.model.Alert;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AlertPersistenceAdapter implements AlertRepositoryPort {

    private final SpringDataAlertRepository repository;
    private final SpringDataCustomerRepository customerRepository;
    private final SpringDataAccountRepository accountRepository;

    public AlertPersistenceAdapter(SpringDataAlertRepository repository,
                                   SpringDataCustomerRepository customerRepository,
                                   SpringDataAccountRepository accountRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Alert save(Alert alert) {
        CustomerJpaEntity customer = alert.getCustomerId() == null ? null
                : customerRepository.getReferenceById(alert.getCustomerId());
        AccountJpaEntity account = alert.getAccountId() == null ? null
                : accountRepository.getReferenceById(alert.getAccountId());
        AlertJpaEntity entity = new AlertJpaEntity(alert.getId(), customer, account,
                alert.getRuleType(), alert.getScore(), alert.getExplanation(),
                alert.getEvidenceTransactionIds(), alert.getStatus(), alert.getCreatedDate(),
                alert.getUpdatedDate());
        return repository.save(entity).toDomain();
    }

    @Override
    public List<Alert> findAll() {
        return repository.findAllByOrderByScoreDescCreatedDateAsc().stream()
            .map(AlertJpaEntity::toDomain).toList();
    }

    @Override
    public List<Alert> findByStatus(String status) {
        return repository.findByStatusOrderByScoreDescCreatedDateAsc(status).stream()
                .map(AlertJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Alert> findById(Long id) {
        return repository.findById(id).map(AlertJpaEntity::toDomain);
    }

    @Override
    public List<Alert> findOpenByAccountIdAndRuleType(Long accountId, String ruleType) {
        return repository.findByAccount_IdAndRuleTypeAndStatus(accountId, ruleType, "OPEN")
                .stream().map(AlertJpaEntity::toDomain).toList();
    }

    @Override
    public List<Alert> findOpenByAccountId(Long accountId) {
        return repository.findByAccount_IdAndStatus(accountId, "OPEN")
                .stream().map(AlertJpaEntity::toDomain).toList();
    }
}
