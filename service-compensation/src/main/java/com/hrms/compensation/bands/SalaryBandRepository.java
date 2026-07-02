package com.hrms.compensation.bands;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalaryBandRepository extends JpaRepository<SalaryBand, UUID> {
    List<SalaryBand> findByTenantIdAndDeletedFalse(String tenantId);
}
