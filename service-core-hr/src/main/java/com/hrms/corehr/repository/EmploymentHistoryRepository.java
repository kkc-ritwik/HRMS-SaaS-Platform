package com.hrms.corehr.repository;

import com.hrms.corehr.entity.EmploymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistory, UUID> {

    List<EmploymentHistory> findByEmployeeIdAndDeletedFalseOrderByStartDateDesc(UUID employeeId);

    Optional<EmploymentHistory> findByIdAndEmployeeIdAndDeletedFalse(UUID id, UUID employeeId);
}
