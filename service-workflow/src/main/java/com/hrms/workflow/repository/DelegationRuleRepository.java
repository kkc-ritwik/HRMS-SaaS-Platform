package com.hrms.workflow.repository;

import com.hrms.workflow.entity.DelegationRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DelegationRuleRepository extends JpaRepository<DelegationRule, UUID> {

    Optional<DelegationRule> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<DelegationRule> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<DelegationRule> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<DelegationRule> findByTenantIdAndDelegatorIdAndActiveAndDeletedFalse(String tenantId, UUID delegatorId, boolean active);

    List<DelegationRule> findByTenantIdAndDelegateIdAndActiveAndDeletedFalse(String tenantId, UUID delegateId, boolean active);
}
