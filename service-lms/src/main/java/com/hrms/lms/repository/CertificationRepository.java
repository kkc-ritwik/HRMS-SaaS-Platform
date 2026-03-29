package com.hrms.lms.repository;

import com.hrms.lms.entity.Certification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, UUID> {

    Optional<Certification> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Certification> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Certification> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Certification> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);

    List<Certification> findByTenantIdAndStatusAndDeletedFalse(String tenantId, Certification.CertificationStatus status);
}
