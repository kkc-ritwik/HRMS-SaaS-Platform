package com.hrms.compliance.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.compliance.dto.LicenseDto;
import com.hrms.compliance.entity.License;
import com.hrms.compliance.entity.License.LicenseStatus;
import com.hrms.compliance.repository.LicenseRepository;
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
public class LicenseService {

    private final LicenseRepository repository;

    @Transactional
    public LicenseDto.Response create(String tenantId, LicenseDto.CreateRequest request, String currentUser) {
        License entity = new License();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setEmployeeId(request.getEmployeeId());
        entity.setLicenseType(request.getLicenseType());
        entity.setLicenseNumber(request.getLicenseNumber());
        entity.setIssuingAuthority(request.getIssuingAuthority());
        entity.setIssueDate(request.getIssueDate());
        entity.setExpiryDate(request.getExpiryDate());
        entity.setStatus(LicenseStatus.ACTIVE);
        entity.setRenewalReminderDays(request.getRenewalReminderDays() != null ? request.getRenewalReminderDays() : 30);
        entity.setDocumentUrl(request.getDocumentUrl());
        entity.setNotes(request.getNotes());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public LicenseDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<LicenseDto.Response> list(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<LicenseDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LicenseDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return repository.findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LicenseDto.Response> listByStatus(String tenantId, LicenseStatus status) {
        return repository.findByTenantIdAndStatusAndDeletedFalse(tenantId, status)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public LicenseDto.Response update(String tenantId, UUID id, LicenseDto.UpdateRequest request, String currentUser) {
        License entity = findOrThrow(tenantId, id);
        if (request.getLicenseType() != null) entity.setLicenseType(request.getLicenseType());
        if (request.getLicenseNumber() != null) entity.setLicenseNumber(request.getLicenseNumber());
        if (request.getIssuingAuthority() != null) entity.setIssuingAuthority(request.getIssuingAuthority());
        if (request.getIssueDate() != null) entity.setIssueDate(request.getIssueDate());
        if (request.getExpiryDate() != null) entity.setExpiryDate(request.getExpiryDate());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getRenewalReminderDays() != null) entity.setRenewalReminderDays(request.getRenewalReminderDays());
        if (request.getDocumentUrl() != null) entity.setDocumentUrl(request.getDocumentUrl());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        License entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    private License findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("License", "id", id));
    }

    private LicenseDto.Response toResponse(License e) {
        return LicenseDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .employeeId(e.getEmployeeId())
                .licenseType(e.getLicenseType())
                .licenseNumber(e.getLicenseNumber())
                .issuingAuthority(e.getIssuingAuthority())
                .issueDate(e.getIssueDate())
                .expiryDate(e.getExpiryDate())
                .status(e.getStatus())
                .renewalReminderDays(e.getRenewalReminderDays())
                .documentUrl(e.getDocumentUrl())
                .notes(e.getNotes())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
