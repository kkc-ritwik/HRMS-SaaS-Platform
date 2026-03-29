package com.hrms.payroll.repository;

import com.hrms.payroll.entity.EsiConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EsiConfigRepository extends JpaRepository<EsiConfig, UUID> {

    /** Latest effective ESI config for a tenant. */
    Optional<EsiConfig> findFirstByTenantIdAndDeletedFalseOrderByEffectiveFromDesc(String tenantId);

    Optional<EsiConfig> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);
}
