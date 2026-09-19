package com.starter.development.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAuditEventRepository extends JpaRepository<AuditEventJpaEntity, Long> {

    List<AuditEventJpaEntity> findByEntityIdOrderByIdAsc(Long entityId);
}
