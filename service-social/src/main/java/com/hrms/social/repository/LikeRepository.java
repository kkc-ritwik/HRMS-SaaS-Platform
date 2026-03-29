package com.hrms.social.repository;

import com.hrms.social.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {

    Optional<Like> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Like> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Like> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Like> findByTenantIdAndPostIdAndDeletedFalse(String tenantId, UUID postId);

    Optional<Like> findByTenantIdAndPostIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID postId, UUID employeeId);

    long countByTenantIdAndPostIdAndDeletedFalse(String tenantId, UUID postId);
}
