package com.hrms.compliance.statutory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatutoryReturnRepository extends JpaRepository<StatutoryReturn, UUID> {

    Optional<StatutoryReturn> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<StatutoryReturn> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<StatutoryReturn> findByTenantIdAndReturnTypeAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, StatutoryReturn.ReturnType returnType);
}
