package com.hrms.payroll.repository;

import com.hrms.payroll.entity.EmployeeSalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmployeeSalaryComponentRepository extends JpaRepository<EmployeeSalaryComponent, UUID> {

    List<EmployeeSalaryComponent> findByEmployeeSalaryId(UUID employeeSalaryId);

    @Modifying
    @Query("DELETE FROM EmployeeSalaryComponent esc WHERE esc.employeeSalaryId = :salaryId")
    void deleteByEmployeeSalaryId(@Param("salaryId") UUID salaryId);
}
