package com.starter.development.adapter.out.persistence;

import com.starter.development.application.port.out.CaseRepositoryPort;
import com.starter.development.domain.model.Case;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CasePersistenceAdapter implements CaseRepositoryPort {

    private final SpringDataCaseRepository repository;
    private final SpringDataAlertRepository alertRepository;

    public CasePersistenceAdapter(SpringDataCaseRepository repository,
                                  SpringDataAlertRepository alertRepository) {
        this.repository = repository;
        this.alertRepository = alertRepository;
    }

    @Override
    public Case save(Case caseFile) {
        AlertJpaEntity alert = alertRepository.getReferenceById(caseFile.getAlertId());
        CaseJpaEntity entity = new CaseJpaEntity(caseFile.getId(), caseFile.getTitle(),
                caseFile.getDescription(), caseFile.getStatus(), alert, caseFile.getAnalystId(),
                caseFile.getDisposition(), caseFile.getNotes(), caseFile.getCreatedAt(),
                caseFile.getUpdatedAt());
        return repository.save(entity).toDomain();
    }

    @Override
    public List<Case> findAll() {
        return repository.findAll().stream().map(CaseJpaEntity::toDomain).toList();
    }

    @Override
    public Optional<Case> findById(Long id) {
        return repository.findById(id).map(CaseJpaEntity::toDomain);
    }

    @Override
    public Optional<Case> findByAlertId(Long alertId) {
        return repository.findByAlert_Id(alertId).map(CaseJpaEntity::toDomain);
    }
}
