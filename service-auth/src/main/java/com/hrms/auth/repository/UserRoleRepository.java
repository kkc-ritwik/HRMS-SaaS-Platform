package com.hrms.auth.repository;

import com.hrms.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUserId(UUID userId);

    @Transactional
    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);
}
