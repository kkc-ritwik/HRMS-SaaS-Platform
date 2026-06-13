package com.hrms.leave.biometric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BiometricDeviceRepository extends JpaRepository<BiometricDevice, UUID> {
    Optional<BiometricDevice> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);
    List<BiometricDevice> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
}
