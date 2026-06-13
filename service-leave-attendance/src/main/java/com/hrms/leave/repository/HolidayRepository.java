package com.hrms.leave.repository;

import com.hrms.leave.entity.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    Optional<Holiday> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Holiday> findByTenantIdAndYearAndDeletedFalse(String tenantId, int year, Pageable pageable);

    List<Holiday> findByTenantIdAndYearAndActiveAndDeletedFalse(
            String tenantId, int year, boolean active);

    List<Holiday> findByTenantIdAndYearAndTypeAndActiveAndDeletedFalse(
            String tenantId, int year, Holiday.HolidayType type, boolean active);

    List<Holiday> findByTenantIdAndDateBetweenAndActiveAndDeletedFalse(
            String tenantId, LocalDate from, LocalDate to, boolean active);

    /** Holidays that either have no location restriction, or include the given location UUID. */
    @Query(value =
           "SELECT * FROM holidays h " +
           "WHERE h.tenant_id = :tenantId AND h.year = :year " +
           "AND h.is_active = true AND h.is_deleted = false " +
           "AND (h.location_ids IS NULL " +
           "     OR h.location_ids @> to_jsonb((:locationId)::uuid))",
           nativeQuery = true)
    List<Holiday> findByLocationAndYear(
            @Param("tenantId") String tenantId,
            @Param("locationId") UUID locationId,
            @Param("year") int year);
}
