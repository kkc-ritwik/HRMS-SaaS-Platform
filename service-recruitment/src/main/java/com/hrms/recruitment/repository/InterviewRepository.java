package com.hrms.recruitment.repository;

import com.hrms.recruitment.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    Optional<Interview> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<Interview> findByApplicationIdAndTenantIdAndDeletedFalseOrderByRoundNumberAsc(
            UUID applicationId, String tenantId);

    List<Interview> findByTenantIdAndStatusAndDeletedFalse(
            String tenantId, Interview.InterviewStatus status);
}
