package com.hrms.performance.repository;

import com.hrms.performance.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    Optional<Feedback> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    /** Feedback received by an employee (respects visibility). */
    @Query("SELECT f FROM Feedback f WHERE f.tenantId = :tenantId " +
           "AND f.toEmployeeId = :employeeId AND f.deleted = false " +
           "AND (f.visibility = 'PUBLIC' OR f.visibility = 'PRIVATE' " +
           "     OR (f.visibility = 'MANAGER_ONLY' AND :isManager = true))")
    Page<Feedback> findReceived(@Param("tenantId")   String  tenantId,
                                @Param("employeeId") UUID    employeeId,
                                @Param("isManager")  boolean isManager,
                                Pageable pageable);

    /** Feedback given by an employee. */
    Page<Feedback> findByTenantIdAndFromEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, UUID fromEmployeeId, Pageable pageable);

    /** Public wall feed for team recognition. */
    @Query("SELECT f FROM Feedback f WHERE f.tenantId = :tenantId AND f.deleted = false " +
           "AND f.visibility = 'PUBLIC' AND f.feedbackType = 'APPRECIATION' " +
           "ORDER BY f.createdAt DESC")
    Page<Feedback> findPublicAppreciations(@Param("tenantId") String tenantId, Pageable pageable);
}
