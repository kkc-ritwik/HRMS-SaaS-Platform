package com.hrms.asset.amc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AmcContractRepository extends JpaRepository<AmcContract, UUID> {

    Optional<AmcContract> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<AmcContract> findByTenantIdAndDeletedFalseOrderByEndDateAsc(String tenantId);

    List<AmcContract> findByTenantIdAndDeletedFalseAndEndDateBeforeOrderByEndDateAsc(String tenantId, LocalDate before);
}
