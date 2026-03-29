package com.hrms.lms.repository;

import com.hrms.lms.entity.CourseModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {

    Optional<CourseModule> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<CourseModule> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<CourseModule> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<CourseModule> findByTenantIdAndCourseIdAndDeletedFalseOrderByOrderIndexAsc(String tenantId, UUID courseId);
}
