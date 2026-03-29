package com.hrms.payroll.repository;

import com.hrms.payroll.entity.PayrollRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {

    Optional<PayrollRun> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<PayrollRun> findByTenantIdAndDeletedFalseOrderByYearDescMonthDesc(
            String tenantId, Pageable pageable);

    List<PayrollRun> findByTenantIdAndYearAndMonthAndDeletedFalse(
            String tenantId, int year, int month);

    boolean existsByTenantIdAndYearAndMonthAndRunTypeAndDeletedFalse(
            String tenantId, int year, int month, PayrollRun.RunType runType);
}
