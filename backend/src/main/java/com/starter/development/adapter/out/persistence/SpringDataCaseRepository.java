package com.starter.development.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCaseRepository extends JpaRepository<CaseJpaEntity, Long> {

    Optional<CaseJpaEntity> findByAlert_Id(Long alertId);
}
