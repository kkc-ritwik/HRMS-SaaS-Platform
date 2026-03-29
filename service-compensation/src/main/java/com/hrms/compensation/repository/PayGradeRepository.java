package com.hrms.compensation.repository;

import com.hrms.compensation.entity.PayGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayGradeRepository extends JpaRepository<PayGrade, UUID> {

    Optional<PayGrade> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<PayGrade> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<PayGrade> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<PayGrade> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);
}
