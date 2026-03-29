package com.hrms.offboarding.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.offboarding.dto.SeparationDto;
import com.hrms.offboarding.entity.Separation;
import com.hrms.offboarding.entity.Separation.SeparationStatus;
import com.hrms.offboarding.repository.SeparationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeparationService {

    private final SeparationRepository separationRepository;

    @Transactional
    public SeparationDto.Response create(String tenantId, SeparationDto.CreateRequest req, String currentUser) {
        Separation separation = new Separation();
        separation.setTenantId(tenantId);
        separation.setEmployeeId(req.getEmployeeId());
        separation.setSeparationType(req.getSeparationType());
        separation.setLastWorkingDate(req.getLastWorkingDate());
        separation.setNoticeDate(req.getNoticeDate());
        separation.setReason(req.getReason());
        separation.setStatus(SeparationStatus.INITIATED);
        separation.setInitiatedBy(req.getInitiatedBy());
        separation.setCreatedBy(currentUser);
        separation.setUpdatedBy(currentUser);
        return toResponse(separationRepository.save(separation));
    }

    @Transactional(readOnly = true)
    public SeparationDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<SeparationDto.Response> list(String tenantId, Pageable pageable) {
        return separationRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SeparationDto.Response update(String tenantId, UUID id, SeparationDto.UpdateRequest req, String currentUser) {
        Separation separation = getEntity(tenantId, id);
        if (req.getSeparationType() != null) {
            separation.setSeparationType(req.getSeparationType());
        }
        if (req.getLastWorkingDate() != null) {
            separation.setLastWorkingDate(req.getLastWorkingDate());
        }
        if (req.getNoticeDate() != null) {
            separation.setNoticeDate(req.getNoticeDate());
        }
        if (req.getReason() != null) {
            separation.setReason(req.getReason());
        }
        if (req.getStatus() != null) {
            separation.setStatus(req.getStatus());
        }
        if (req.getApprovedBy() != null) {
            separation.setApprovedBy(req.getApprovedBy());
        }
        if (req.getFinalSettlementDate() != null) {
            separation.setFinalSettlementDate(req.getFinalSettlementDate());
        }
        if (req.getNotes() != null) {
            separation.setNotes(req.getNotes());
        }
        separation.setUpdatedBy(currentUser);
        return toResponse(separationRepository.save(separation));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Separation separation = getEntity(tenantId, id);
        separation.setDeleted(true);
        separation.setUpdatedBy(currentUser);
        separationRepository.save(separation);
    }

    @Transactional(readOnly = true)
    public List<SeparationDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return separationRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SeparationDto.Response> listByStatus(String tenantId, SeparationStatus status, Pageable pageable) {
        return separationRepository
                .findByTenantIdAndStatusAndDeletedFalse(tenantId, status, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SeparationDto.Response approve(String tenantId, UUID id, UUID approvedBy, String currentUser) {
        Separation separation = getEntity(tenantId, id);
        separation.setStatus(SeparationStatus.IN_PROGRESS);
        separation.setApprovedBy(approvedBy);
        separation.setUpdatedBy(currentUser);
        return toResponse(separationRepository.save(separation));
    }

    @Transactional
    public SeparationDto.Response complete(String tenantId, UUID id, LocalDate finalSettlementDate, String currentUser) {
        Separation separation = getEntity(tenantId, id);
        separation.setStatus(SeparationStatus.COMPLETED);
        separation.setFinalSettlementDate(finalSettlementDate);
        separation.setUpdatedBy(currentUser);
        return toResponse(separationRepository.save(separation));
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

    private Separation getEntity(String tenantId, UUID id) {
        return separationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Separation", "id", id));
    }

    private SeparationDto.Response toResponse(Separation s) {
        return SeparationDto.Response.builder()
                .id(s.getId())
                .tenantId(s.getTenantId())
                .employeeId(s.getEmployeeId())
                .separationType(s.getSeparationType())
                .lastWorkingDate(s.getLastWorkingDate())
                .noticeDate(s.getNoticeDate())
                .reason(s.getReason())
                .status(s.getStatus())
                .initiatedBy(s.getInitiatedBy())
                .approvedBy(s.getApprovedBy())
                .finalSettlementDate(s.getFinalSettlementDate())
                .notes(s.getNotes())
                .createdBy(s.getCreatedBy())
                .updatedBy(s.getUpdatedBy())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
