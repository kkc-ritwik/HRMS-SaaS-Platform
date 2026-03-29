package com.hrms.document.repository;

import com.hrms.document.entity.GeneratedLetter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GeneratedLetterRepository extends JpaRepository<GeneratedLetter, UUID> {

    Optional<GeneratedLetter> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<GeneratedLetter> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<GeneratedLetter> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<GeneratedLetter> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, UUID employeeId);
}
