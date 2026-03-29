package com.hrms.leave.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.leave.dto.LeaveTypeDto;
import com.hrms.leave.entity.LeaveType;
import com.hrms.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Transactional
    public LeaveTypeDto.Response create(String tenantId, LeaveTypeDto.CreateRequest req,
                                         String currentUser) {
        if (leaveTypeRepository.existsByCodeAndTenantIdAndDeletedFalse(req.getCode(), tenantId)) {
            throw new DuplicateResourceException("LeaveType", "code", req.getCode());
        }
        LeaveType entity = new LeaveType();
        entity.setTenantId(tenantId);
        entity.setName(req.getName());
        entity.setCode(req.getCode().toUpperCase());
        entity.setPaid(req.isPaid());
        entity.setColor(req.getColor());
        entity.setDescription(req.getDescription());
        entity.setAppliesToGender(req.getAppliesToGender());
        entity.setAppliesToEmploymentType(req.getAppliesToEmploymentType());
        entity.setMaxDaysPerYear(req.getMaxDaysPerYear());
        entity.setRequiresAttachmentAfterDays(req.getRequiresAttachmentAfterDays());
        entity.setActive(true);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(leaveTypeRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<LeaveTypeDto.Response> list(String tenantId, Pageable pageable) {
        return leaveTypeRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeDto.Response> listActive(String tenantId) {
        return leaveTypeRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LeaveTypeDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional
    public LeaveTypeDto.Response update(String tenantId, UUID id,
                                         LeaveTypeDto.UpdateRequest req, String currentUser) {
        LeaveType entity = findOrThrow(tenantId, id);
        if (req.getName() != null)                      entity.setName(req.getName());
        if (req.getPaid() != null)                      entity.setPaid(req.getPaid());
        if (req.getColor() != null)                     entity.setColor(req.getColor());
        if (req.getDescription() != null)               entity.setDescription(req.getDescription());
        if (req.getAppliesToGender() != null)           entity.setAppliesToGender(req.getAppliesToGender());
        if (req.getAppliesToEmploymentType() != null)   entity.setAppliesToEmploymentType(req.getAppliesToEmploymentType());
        if (req.getMaxDaysPerYear() != null)            entity.setMaxDaysPerYear(req.getMaxDaysPerYear());
        if (req.getRequiresAttachmentAfterDays() != null) entity.setRequiresAttachmentAfterDays(req.getRequiresAttachmentAfterDays());
        if (req.getActive() != null)                    entity.setActive(req.getActive());
        entity.setUpdatedBy(currentUser);
        return toResponse(leaveTypeRepository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        LeaveType entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        leaveTypeRepository.save(entity);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize()).total(page.getTotalElements())
                .totalPages(page.getTotalPages()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── package-visible helpers ───────────────────────────────────────────────

    LeaveType findOrThrow(String tenantId, UUID id) {
        return leaveTypeRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));
    }

    LeaveTypeDto.Response toResponse(LeaveType e) {
        return LeaveTypeDto.Response.builder()
                .id(e.getId()).tenantId(e.getTenantId())
                .name(e.getName()).code(e.getCode())
                .paid(e.isPaid()).color(e.getColor()).description(e.getDescription())
                .appliesToGender(e.getAppliesToGender())
                .appliesToEmploymentType(e.getAppliesToEmploymentType())
                .maxDaysPerYear(e.getMaxDaysPerYear())
                .requiresAttachmentAfterDays(e.getRequiresAttachmentAfterDays())
                .active(e.isActive())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
