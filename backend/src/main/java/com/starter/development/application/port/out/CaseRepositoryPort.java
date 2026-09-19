package com.starter.development.application.port.out;

import com.starter.development.domain.model.Case;

import java.util.List;
import java.util.Optional;

public interface CaseRepositoryPort {

    Case save(Case caseFile);

    List<Case> findAll();

    Optional<Case> findById(Long id);

    Optional<Case> findByAlertId(Long alertId);
}
