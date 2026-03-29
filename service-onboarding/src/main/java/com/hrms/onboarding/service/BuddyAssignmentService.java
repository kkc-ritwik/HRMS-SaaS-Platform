package com.hrms.onboarding.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.onboarding.dto.BuddyAssignmentDto;
import com.hrms.onboarding.entity.BuddyAssignment;
import com.hrms.onboarding.entity.BuddyAssignment.AssignmentStatus;
import com.hrms.onboarding.repository.BuddyAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuddyAssignmentService {

    private final BuddyAssignmentRepository buddyAssignmentRepository;

    @Transactional
    public BuddyAssignmentDto.Response create(String tenantId, BuddyAssignmentDto.CreateRequest req, String currentUser) {
        BuddyAssignment assignment = new BuddyAssignment();
        assignment.setTenantId(tenantId);
        assignment.setEmployeeId(req.getEmployeeId());
        assignment.setBuddyId(req.getBuddyId());
        assignment.setStartDate(req.getStartDate());
        assignment.setEndDate(req.getEndDate());
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setNotes(req.getNotes());
        assignment.setCreatedBy(currentUser);
        assignment.setUpdatedBy(currentUser);
        return toResponse(buddyAssignmentRepository.save(assignment));
    }

    @Transactional
    public BuddyAssignmentDto.Response update(String tenantId, UUID id, BuddyAssignmentDto.UpdateRequest req, String currentUser) {
        BuddyAssignment assignment = getEntity(tenantId, id);
        if (req.getEmployeeId() != null) {
            assignment.setEmployeeId(req.getEmployeeId());
        }
        if (req.getBuddyId() != null) {
            assignment.setBuddyId(req.getBuddyId());
        }
        if (req.getStartDate() != null) {
            assignment.setStartDate(req.getStartDate());
        }
        if (req.getEndDate() != null) {
            assignment.setEndDate(req.getEndDate());
        }
        if (req.getStatus() != null) {
            assignment.setStatus(req.getStatus());
        }
        if (req.getNotes() != null) {
            assignment.setNotes(req.getNotes());
        }
        assignment.setUpdatedBy(currentUser);
        return toResponse(buddyAssignmentRepository.save(assignment));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        BuddyAssignment assignment = getEntity(tenantId, id);
        assignment.setDeleted(true);
        assignment.setUpdatedBy(currentUser);
        buddyAssignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public BuddyAssignmentDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<BuddyAssignmentDto.Response> list(String tenantId, Pageable pageable) {
        return buddyAssignmentRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<BuddyAssignmentDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return buddyAssignmentRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private BuddyAssignment getEntity(String tenantId, UUID id) {
        return buddyAssignmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("BuddyAssignment", "id", id));
    }

    private BuddyAssignmentDto.Response toResponse(BuddyAssignment assignment) {
        return BuddyAssignmentDto.Response.builder()
                .id(assignment.getId())
                .tenantId(assignment.getTenantId())
                .employeeId(assignment.getEmployeeId())
                .buddyId(assignment.getBuddyId())
                .startDate(assignment.getStartDate())
                .endDate(assignment.getEndDate())
                .status(assignment.getStatus())
                .notes(assignment.getNotes())
                .createdBy(assignment.getCreatedBy())
                .updatedBy(assignment.getUpdatedBy())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
