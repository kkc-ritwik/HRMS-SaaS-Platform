package com.hrms.corehr.repository;

import com.hrms.corehr.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, UUID> {

    List<FamilyMember> findByEmployeeIdAndDeletedFalse(UUID employeeId);

    long countByEmployeeIdAndDeletedFalse(UUID employeeId);

    Optional<FamilyMember> findByIdAndEmployeeIdAndDeletedFalse(UUID id, UUID employeeId);
}
