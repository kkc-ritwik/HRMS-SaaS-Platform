package com.hrms.onboarding.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.onboarding.dto.OnboardingDocumentDto;
import com.hrms.onboarding.entity.OnboardingDocument;
import com.hrms.onboarding.entity.OnboardingDocument.DocumentStatus;
import com.hrms.onboarding.repository.OnboardingDocumentRepository;
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
public class OnboardingDocumentService {

    private final OnboardingDocumentRepository onboardingDocumentRepository;

    @Transactional
    public OnboardingDocumentDto.Response create(String tenantId, OnboardingDocumentDto.CreateRequest req, String currentUser) {
        OnboardingDocument document = new OnboardingDocument();
        document.setTenantId(tenantId);
        document.setEmployeeId(req.getEmployeeId());
        document.setDocumentName(req.getDocumentName());
        document.setDocumentType(req.getDocumentType());
        document.setFileUrl(req.getFileUrl());
        document.setStatus(req.getStatus() != null ? req.getStatus() : DocumentStatus.PENDING);
        document.setRemarks(req.getRemarks());
        document.setCreatedBy(currentUser);
        document.setUpdatedBy(currentUser);
        return toResponse(onboardingDocumentRepository.save(document));
    }

    @Transactional
    public OnboardingDocumentDto.Response update(String tenantId, UUID id, OnboardingDocumentDto.UpdateRequest req, String currentUser) {
        OnboardingDocument document = getEntity(tenantId, id);
        if (req.getEmployeeId() != null) {
            document.setEmployeeId(req.getEmployeeId());
        }
        if (req.getDocumentName() != null) {
            document.setDocumentName(req.getDocumentName());
        }
        if (req.getDocumentType() != null) {
            document.setDocumentType(req.getDocumentType());
        }
        if (req.getFileUrl() != null) {
            document.setFileUrl(req.getFileUrl());
        }
        if (req.getStatus() != null) {
            document.setStatus(req.getStatus());
        }
        if (req.getRemarks() != null) {
            document.setRemarks(req.getRemarks());
        }
        document.setUpdatedBy(currentUser);
        return toResponse(onboardingDocumentRepository.save(document));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        OnboardingDocument document = getEntity(tenantId, id);
        document.setDeleted(true);
        document.setUpdatedBy(currentUser);
        onboardingDocumentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public OnboardingDocumentDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<OnboardingDocumentDto.Response> list(String tenantId, Pageable pageable) {
        return onboardingDocumentRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OnboardingDocumentDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return onboardingDocumentRepository
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

    private OnboardingDocument getEntity(String tenantId, UUID id) {
        return onboardingDocumentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("OnboardingDocument", "id", id));
    }

    private OnboardingDocumentDto.Response toResponse(OnboardingDocument document) {
        return OnboardingDocumentDto.Response.builder()
                .id(document.getId())
                .tenantId(document.getTenantId())
                .employeeId(document.getEmployeeId())
                .documentName(document.getDocumentName())
                .documentType(document.getDocumentType())
                .fileUrl(document.getFileUrl())
                .status(document.getStatus())
                .remarks(document.getRemarks())
                .createdBy(document.getCreatedBy())
                .updatedBy(document.getUpdatedBy())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
