package com.hrms.auth.repository;

import com.hrms.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    List<Role> findByTenantIdAndActiveTrue(String tenantId);

    Optional<Role> findByCodeAndTenantId(String code, String tenantId);
}
