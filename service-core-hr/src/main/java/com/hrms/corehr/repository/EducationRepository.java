package com.hrms.corehr.repository;

import com.hrms.corehr.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EducationRepository extends JpaRepository<Education, UUID> {

    List<Education> findByEmployeeIdAndDeletedFalseOrderByEndYearDesc(UUID employeeId);

    Optional<Education> findByIdAndEmployeeIdAndDeletedFalse(UUID id, UUID employeeId);
}
