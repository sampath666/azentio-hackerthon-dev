package com.starter.development.application.port.out;

import com.starter.development.domain.model.Alert;

import java.util.List;
import java.util.Optional;

public interface AlertRepositoryPort {

    Alert save(Alert alert);

    List<Alert> findAll();

    List<Alert> findByStatus(String status);

    Optional<Alert> findById(Long id);

    List<Alert> findOpenByAccountIdAndRuleType(Long accountId, String ruleType);

    List<Alert> findOpenByAccountId(Long accountId);
}
