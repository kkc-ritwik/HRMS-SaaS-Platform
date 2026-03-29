package com.hrms.auth.repository;

import com.hrms.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndTenantIdAndDeletedFalse(String email, String tenantId);

    boolean existsByEmailAndTenantId(String email, String tenantId);

    Page<User> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    Optional<User> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);
}
