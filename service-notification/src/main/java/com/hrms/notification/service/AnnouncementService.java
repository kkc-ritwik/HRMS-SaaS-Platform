package com.hrms.notification.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.notification.dto.AnnouncementDto;
import com.hrms.notification.entity.Announcement;
import com.hrms.notification.entity.Announcement.AudienceType;
import com.hrms.notification.entity.Announcement.Priority;
import com.hrms.notification.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementDto.Response create(String tenantId, String currentUser,
                                           AnnouncementDto.CreateRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTenantId(tenantId);
        announcement.setCreatedBy(currentUser);
        announcement.setUpdatedBy(currentUser);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setAudienceType(
                request.getAudienceType() != null ? request.getAudienceType() : AudienceType.ALL);
        announcement.setTargetIds(request.getTargetIds());
        announcement.setPriority(
                request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        announcement.setStartDate(request.getStartDate());
        announcement.setEndDate(request.getEndDate());
        announcement.setAuthorId(request.getAuthorId());
        announcement.setPinned(request.isPinned());
        return toResponse(announcementRepository.save(announcement));
    }

    @Transactional(readOnly = true)
    public AnnouncementDto.Response getById(String tenantId, UUID id) {
        Announcement announcement = announcementRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
        return toResponse(announcement);
    }

    @Transactional(readOnly = true)
    public Page<AnnouncementDto.Response> list(String tenantId, Pageable pageable) {
        return announcementRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    public AnnouncementDto.Response update(String tenantId, UUID id, String currentUser,
                                           AnnouncementDto.UpdateRequest request) {
        Announcement announcement = announcementRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            announcement.setContent(request.getContent());
        }
        if (request.getAudienceType() != null) {
            announcement.setAudienceType(request.getAudienceType());
        }
        if (request.getTargetIds() != null) {
            announcement.setTargetIds(request.getTargetIds());
        }
        if (request.getPriority() != null) {
            announcement.setPriority(request.getPriority());
        }
        if (request.getStartDate() != null) {
            announcement.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            announcement.setEndDate(request.getEndDate());
        }
        if (request.getAuthorId() != null) {
            announcement.setAuthorId(request.getAuthorId());
        }
        if (request.getPinned() != null) {
            announcement.setPinned(request.getPinned());
        }
        announcement.setUpdatedBy(currentUser);
        return toResponse(announcementRepository.save(announcement));
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        Announcement announcement = announcementRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
        announcement.setDeleted(true);
        announcement.setUpdatedBy(currentUser);
        announcementRepository.save(announcement);
    }

    private AnnouncementDto.Response toResponse(Announcement a) {
        return AnnouncementDto.Response.builder()
                .id(a.getId())
                .tenantId(a.getTenantId())
                .title(a.getTitle())
                .content(a.getContent())
                .audienceType(a.getAudienceType())
                .targetIds(a.getTargetIds())
                .priority(a.getPriority())
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .authorId(a.getAuthorId())
                .pinned(a.isPinned())
                .createdBy(a.getCreatedBy())
                .updatedBy(a.getUpdatedBy())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
