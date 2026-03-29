package com.hrms.corehr.repository;

import com.hrms.corehr.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    Page<Location> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    List<Location> findByTenantIdAndDeletedFalse(String tenantId);

    Optional<Location> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    boolean existsByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);

    boolean existsByCodeAndTenantIdAndDeletedFalseAndIdNot(String code, String tenantId, UUID id);

    List<Location> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);

    @Query("SELECT l FROM Location l WHERE l.tenantId = :tenantId AND l.deleted = false " +
           "AND (:search IS NULL OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Location> searchByTenant(@Param("tenantId") String tenantId,
                                   @Param("search") String search,
                                   Pageable pageable);

    long countByTenantIdAndDeletedFalse(String tenantId);
}
