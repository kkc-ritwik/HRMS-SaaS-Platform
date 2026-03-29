package com.hrms.payroll.repository;

import com.hrms.payroll.entity.TaxDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxDeclarationRepository extends JpaRepository<TaxDeclaration, UUID> {

    Optional<TaxDeclaration> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Optional<TaxDeclaration> findByEmployeeIdAndTenantIdAndFinancialYearAndDeletedFalse(
            UUID employeeId, String tenantId, String financialYear);

    List<TaxDeclaration> findByEmployeeIdAndTenantIdAndDeletedFalseOrderByFinancialYearDesc(
            UUID employeeId, String tenantId);

    List<TaxDeclaration> findByTenantIdAndFinancialYearAndStatusAndDeletedFalse(
            String tenantId, String financialYear, TaxDeclaration.DeclarationStatus status);
}
