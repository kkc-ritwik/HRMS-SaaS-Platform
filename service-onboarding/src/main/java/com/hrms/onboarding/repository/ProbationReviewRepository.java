package com.hrms.onboarding.repository;

import com.hrms.onboarding.entity.ProbationReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProbationReviewRepository extends JpaRepository<ProbationReview, UUID> {

    Page<ProbationReview> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ProbationReview> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Optional<ProbationReview> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<ProbationReview> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);
}
