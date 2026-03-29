package com.hrms.recruitment.repository;

import com.hrms.recruitment.entity.RecruitmentAgency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecruitmentAgencyRepository extends JpaRepository<RecruitmentAgency, UUID> {

    Optional<RecruitmentAgency> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<RecruitmentAgency> findByTenantIdAndDeletedFalseOrderByNameAsc(String tenantId, Pageable pageable);

    boolean existsByTenantIdAndNameAndDeletedFalse(String tenantId, String name);
}
