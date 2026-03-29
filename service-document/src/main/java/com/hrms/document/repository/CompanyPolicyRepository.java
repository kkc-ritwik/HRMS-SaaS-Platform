package com.hrms.document.repository;

import com.hrms.document.entity.CompanyPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyPolicyRepository extends JpaRepository<CompanyPolicy, UUID> {

    Optional<CompanyPolicy> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<CompanyPolicy> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<CompanyPolicy> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
}
