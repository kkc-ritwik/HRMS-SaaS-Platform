package com.hrms.social.repository;

import com.hrms.social.entity.GroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    Optional<GroupMember> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<GroupMember> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<GroupMember> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<GroupMember> findByTenantIdAndGroupIdAndDeletedFalse(String tenantId, UUID groupId);

    List<GroupMember> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);

    Optional<GroupMember> findByGroupIdAndEmployeeIdAndTenantIdAndDeletedFalse(UUID groupId, UUID employeeId, String tenantId);
}
