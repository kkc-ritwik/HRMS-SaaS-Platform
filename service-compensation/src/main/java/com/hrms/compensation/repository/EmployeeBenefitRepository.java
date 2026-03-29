package com.hrms.compensation.repository;

import com.hrms.compensation.entity.EmployeeBenefit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeBenefitRepository extends JpaRepository<EmployeeBenefit, UUID> {

    Optional<EmployeeBenefit> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<EmployeeBenefit> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<EmployeeBenefit> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<EmployeeBenefit> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);

    List<EmployeeBenefit> findByTenantIdAndBenefitIdAndDeletedFalse(String tenantId, UUID benefitId);
}
