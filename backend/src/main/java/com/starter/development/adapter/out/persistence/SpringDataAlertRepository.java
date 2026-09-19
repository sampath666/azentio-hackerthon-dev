package com.starter.development.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAlertRepository extends JpaRepository<AlertJpaEntity, Long> {

    List<AlertJpaEntity> findAllByOrderByScoreDescCreatedDateAsc();

    List<AlertJpaEntity> findByStatusOrderByScoreDescCreatedDateAsc(String status);

    List<AlertJpaEntity> findByAccount_IdAndRuleTypeAndStatus(
            Long accountId, String ruleType, String status);

    List<AlertJpaEntity> findByAccount_IdAndStatus(Long accountId, String status);
}
