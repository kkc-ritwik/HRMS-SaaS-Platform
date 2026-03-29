package com.hrms.payroll.repository;

import com.hrms.payroll.entity.PtSlab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PtSlabRepository extends JpaRepository<PtSlab, UUID> {

    List<PtSlab> findByTenantIdAndStateAndDeletedFalseOrderBySlabFrom(String tenantId, String state);

    Optional<PtSlab> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    /** Find the applicable PT slab for a given monthly salary, state, gender, and date. */
    @Query("SELECT ps FROM PtSlab ps WHERE ps.tenantId = :tenantId AND ps.state = :state " +
           "AND ps.deleted = false AND ps.effectiveFrom <= :asOf " +
           "AND ps.slabFrom <= :salary " +
           "AND (ps.slabTo IS NULL OR ps.slabTo >= :salary) " +
           "AND (ps.gender IS NULL OR ps.gender = :gender) " +
           "ORDER BY ps.effectiveFrom DESC, ps.slabFrom DESC")
    List<PtSlab> findApplicableSlab(@Param("tenantId") String tenantId,
                                     @Param("state")    String state,
                                     @Param("salary")   BigDecimal salary,
                                     @Param("gender")   String gender,
                                     @Param("asOf")     LocalDate asOf);
}
