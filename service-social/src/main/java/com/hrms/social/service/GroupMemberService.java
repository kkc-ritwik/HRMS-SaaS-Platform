package com.hrms.social.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.social.dto.GroupMemberDto;
import com.hrms.social.entity.GroupMember;
import com.hrms.social.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;

    public GroupMemberDto.Response create(String tenantId, GroupMemberDto.CreateRequest request, String currentUser) {
        GroupMember member = new GroupMember();
        member.setTenantId(tenantId);
        member.setGroupId(request.getGroupId());
        member.setEmployeeId(request.getEmployeeId());
        member.setRole(request.getRole() != null ? request.getRole() : GroupMember.MemberRole.MEMBER);
        member.setJoinedAt(Instant.now());
        member.setCreatedBy(currentUser);
        member.setUpdatedBy(currentUser);
        return toResponse(groupMemberRepository.save(member));
    }

    public GroupMemberDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    public Page<GroupMemberDto.Response> list(String tenantId, Pageable pageable) {
        return groupMemberRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    public List<GroupMemberDto.Response> listByGroup(String tenantId, UUID groupId) {
        return groupMemberRepository.findByTenantIdAndGroupIdAndDeletedFalse(tenantId, groupId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<GroupMemberDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return groupMemberRepository.findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public GroupMemberDto.Response addMember(String tenantId, UUID groupId, UUID employeeId,
                                              GroupMember.MemberRole role, String currentUser) {
        GroupMemberDto.CreateRequest request = new GroupMemberDto.CreateRequest();
        request.setGroupId(groupId);
        request.setEmployeeId(employeeId);
        request.setRole(role != null ? role : GroupMember.MemberRole.MEMBER);
        return create(tenantId, request, currentUser);
    }

    public void removeMember(String tenantId, UUID groupId, UUID employeeId, String currentUser) {
        GroupMember member = groupMemberRepository
                .findByGroupIdAndEmployeeIdAndTenantIdAndDeletedFalse(groupId, employeeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", "groupId+employeeId",
                        groupId + "+" + employeeId));
        member.setDeleted(true);
        member.setUpdatedBy(currentUser);
        groupMemberRepository.save(member);
    }

    public GroupMemberDto.Response update(String tenantId, UUID id, GroupMemberDto.UpdateRequest request, String currentUser) {
        GroupMember member = findOrThrow(tenantId, id);
        if (request.getRole() != null) member.setRole(request.getRole());
        member.setUpdatedBy(currentUser);
        return toResponse(groupMemberRepository.save(member));
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        GroupMember member = findOrThrow(tenantId, id);
        member.setDeleted(true);
        member.setUpdatedBy(currentUser);
        groupMemberRepository.save(member);
    }

    private GroupMember findOrThrow(String tenantId, UUID id) {
        return groupMemberRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupMember", "id", id));
    }

    private GroupMemberDto.Response toResponse(GroupMember member) {
        return GroupMemberDto.Response.builder()
                .id(member.getId())
                .tenantId(member.getTenantId())
                .groupId(member.getGroupId())
                .employeeId(member.getEmployeeId())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .createdBy(member.getCreatedBy())
                .updatedBy(member.getUpdatedBy())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
