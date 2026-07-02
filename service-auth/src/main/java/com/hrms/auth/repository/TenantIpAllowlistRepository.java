package com.hrms.auth.repository;

import com.hrms.auth.security.TenantIpAllowlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for tenant IP allowlist CIDR rules. Extracted to a top-level interface
 * (was previously nested inside IpAllowlistFilter, which Spring Data does not reliably
 * pick up as a repository bean).
 */
public interface TenantIpAllowlistRepository extends JpaRepository<TenantIpAllowlist, UUID> {
    List<TenantIpAllowlist> findByTenantIdAndActiveTrue(String tenantId);
}
