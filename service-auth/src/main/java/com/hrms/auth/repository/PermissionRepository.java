package com.hrms.auth.repository;

import com.hrms.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    List<Permission> findByModule(String module);

    Optional<Permission> findByModuleAndAction(String module, String action);
}
