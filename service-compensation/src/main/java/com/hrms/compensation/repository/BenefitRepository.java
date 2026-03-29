package com.hrms.compensation.repository;

import com.hrms.compensation.entity.Benefit;
import com.hrms.compensation.entity.Benefit.BenefitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BenefitRepository extends JpaRepository<Benefit, UUID> {

    Optional<Benefit> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Benefit> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Benefit> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Benefit> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);

    List<Benefit> findByTenantIdAndBenefitTypeAndDeletedFalse(String tenantId, BenefitType benefitType);
}
