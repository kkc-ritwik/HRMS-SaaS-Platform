package com.hrms.payroll.repository;

import com.hrms.payroll.entity.PfConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PfConfigRepository extends JpaRepository<PfConfig, UUID> {

    /** Latest effective PF config for a tenant. */
    Optional<PfConfig> findFirstByTenantIdAndDeletedFalseOrderByEffectiveFromDesc(String tenantId);

    Optional<PfConfig> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);
}
