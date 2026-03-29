package com.hrms.social.repository;

import com.hrms.social.entity.SocialGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialGroupRepository extends JpaRepository<SocialGroup, UUID> {

    Optional<SocialGroup> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<SocialGroup> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<SocialGroup> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<SocialGroup> findByTenantIdAndOwnerIdAndDeletedFalse(String tenantId, UUID ownerId);
}
