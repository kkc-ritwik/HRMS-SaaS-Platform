package com.hrms.payroll.repository;

import com.hrms.payroll.entity.SalaryStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, UUID> {

    Optional<SalaryStructure> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<SalaryStructure> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    List<SalaryStructure> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);

    Optional<SalaryStructure> findFirstByTenantIdAndDefaultStructureTrueAndDeletedFalse(String tenantId);

    boolean existsByNameAndTenantIdAndDeletedFalse(String name, String tenantId);

    boolean existsByNameAndTenantIdAndDeletedFalseAndIdNot(String name, String tenantId, UUID id);
}
