package com.hrms.leave.repository;

import com.hrms.leave.entity.OptionalHolidaySelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OptionalHolidaySelectionRepository extends JpaRepository<OptionalHolidaySelection, UUID> {

    List<OptionalHolidaySelection> findByTenantIdAndEmployeeIdAndYearAndDeletedFalse(
            String tenantId, UUID employeeId, int year);

    void deleteByTenantIdAndEmployeeIdAndYear(String tenantId, UUID employeeId, int year);
}
