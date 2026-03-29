package com.hrms.corehr.repository;

import com.hrms.corehr.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, UUID> {

    List<EmergencyContact> findByEmployeeIdAndDeletedFalse(UUID employeeId);

    Optional<EmergencyContact> findByEmployeeIdAndPrimaryTrueAndDeletedFalse(UUID employeeId);

    long countByEmployeeIdAndDeletedFalse(UUID employeeId);

    Optional<EmergencyContact> findByIdAndEmployeeIdAndDeletedFalse(UUID id, UUID employeeId);
}
