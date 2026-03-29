package com.hrms.social.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.social.dto.SocialGroupDto;
import com.hrms.social.entity.SocialGroup;
import com.hrms.social.repository.SocialGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocialGroupService {

    private final SocialGroupRepository socialGroupRepository;

    public SocialGroupDto.Response create(String tenantId, SocialGroupDto.CreateRequest request, String currentUser) {
        SocialGroup group = new SocialGroup();
        group.setTenantId(tenantId);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setGroupType(request.getGroupType() != null ? request.getGroupType() : SocialGroup.GroupType.PUBLIC);
        group.setOwnerId(request.getOwnerId());
        group.setCoverImageUrl(request.getCoverImageUrl());
        group.setMemberCount(0);
        group.setCreatedBy(currentUser);
        group.setUpdatedBy(currentUser);
        return toResponse(socialGroupRepository.save(group));
    }

    public SocialGroupDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    public Page<SocialGroupDto.Response> list(String tenantId, Pageable pageable) {
        return socialGroupRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    public List<SocialGroupDto.Response> listAll(String tenantId) {
        return socialGroupRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SocialGroupDto.Response update(String tenantId, UUID id, SocialGroupDto.UpdateRequest request, String currentUser) {
        SocialGroup group = findOrThrow(tenantId, id);
        if (request.getName() != null) group.setName(request.getName());
        if (request.getDescription() != null) group.setDescription(request.getDescription());
        if (request.getGroupType() != null) group.setGroupType(request.getGroupType());
        if (request.getCoverImageUrl() != null) group.setCoverImageUrl(request.getCoverImageUrl());
        group.setUpdatedBy(currentUser);
        return toResponse(socialGroupRepository.save(group));
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        SocialGroup group = findOrThrow(tenantId, id);
        group.setDeleted(true);
        group.setUpdatedBy(currentUser);
        socialGroupRepository.save(group);
    }

    private SocialGroup findOrThrow(String tenantId, UUID id) {
        return socialGroupRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SocialGroup", "id", id));
    }

    private SocialGroupDto.Response toResponse(SocialGroup group) {
        return SocialGroupDto.Response.builder()
                .id(group.getId())
                .tenantId(group.getTenantId())
                .name(group.getName())
                .description(group.getDescription())
                .groupType(group.getGroupType())
                .ownerId(group.getOwnerId())
                .coverImageUrl(group.getCoverImageUrl())
                .memberCount(group.getMemberCount())
                .createdBy(group.getCreatedBy())
                .updatedBy(group.getUpdatedBy())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
