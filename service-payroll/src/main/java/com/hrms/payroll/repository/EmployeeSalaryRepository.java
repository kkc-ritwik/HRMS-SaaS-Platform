package com.hrms.payroll.repository;

import com.hrms.payroll.entity.EmployeeSalary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeSalaryRepository extends JpaRepository<EmployeeSalary, UUID> {

    Optional<EmployeeSalary> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    /** Current active salary for an employee. */
    Optional<EmployeeSalary> findFirstByEmployeeIdAndTenantIdAndStatusAndDeletedFalseOrderByEffectiveFromDesc(
            UUID employeeId, String tenantId, EmployeeSalary.SalaryStatus status);

    /** Full revision history for an employee. */
    List<EmployeeSalary> findByEmployeeIdAndTenantIdAndDeletedFalseOrderByEffectiveFromDesc(
            UUID employeeId, String tenantId);

    Page<EmployeeSalary> findByEmployeeIdAndTenantIdAndDeletedFalse(
            UUID employeeId, String tenantId, Pageable pageable);

    /** All active salaries for a tenant — used by payroll run processing. */
    List<EmployeeSalary> findByTenantIdAndStatusAndDeletedFalse(
            String tenantId, EmployeeSalary.SalaryStatus status);

    /** Close the currently-active salary before creating a new revision. */
    @Modifying
    @Query("UPDATE EmployeeSalary es SET es.effectiveTo = :effectiveTo, " +
           "es.status = 'INACTIVE', es.updatedBy = :updatedBy " +
           "WHERE es.employeeId = :employeeId AND es.tenantId = :tenantId " +
           "AND es.status = 'ACTIVE' AND es.deleted = false")
    int closeActiveSalary(@Param("employeeId") UUID employeeId,
                          @Param("tenantId")   String tenantId,
                          @Param("effectiveTo") LocalDate effectiveTo,
                          @Param("updatedBy")   String updatedBy);
}
