package com.hrms.lms.repository;

import com.hrms.lms.entity.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    Optional<Assessment> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Assessment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Assessment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Assessment> findByTenantIdAndCourseIdAndDeletedFalse(String tenantId, UUID courseId);
}
