package com.hrms.lms.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.lms.dto.CertificationDto;
import com.hrms.lms.entity.Certification;
import com.hrms.lms.repository.CertificationRepository;
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
public class CertificationService {

    private final CertificationRepository certificationRepository;

    @Transactional
    public CertificationDto.Response create(String tenantId, CertificationDto.CreateRequest request, String currentUser) {
        Certification certification = new Certification();
        certification.setTenantId(tenantId);
        certification.setCreatedBy(currentUser);
        certification.setUpdatedBy(currentUser);
        certification.setEmployeeId(request.getEmployeeId());
        certification.setCourseId(request.getCourseId());
        certification.setCertificateName(request.getCertificateName());
        certification.setIssuedBy(request.getIssuedBy());
        certification.setIssueDate(request.getIssueDate());
        certification.setExpiryDate(request.getExpiryDate());
        certification.setCertificateUrl(request.getCertificateUrl());
        certification.setStatus(Certification.CertificationStatus.ACTIVE);
        return toResponse(certificationRepository.save(certification));
    }

    @Transactional(readOnly = true)
    public CertificationDto.Response getById(String tenantId, UUID id) {
        Certification certification = certificationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", "id", id));
        return toResponse(certification);
    }

    @Transactional(readOnly = true)
    public Page<CertificationDto.Response> list(String tenantId, Pageable pageable) {
        return certificationRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CertificationDto.Response> listAll(String tenantId) {
        return certificationRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CertificationDto.Response update(String tenantId, UUID id, CertificationDto.UpdateRequest request, String currentUser) {
        Certification certification = certificationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", "id", id));
        if (request.getEmployeeId() != null) certification.setEmployeeId(request.getEmployeeId());
        if (request.getCourseId() != null) certification.setCourseId(request.getCourseId());
        if (request.getCertificateName() != null) certification.setCertificateName(request.getCertificateName());
        if (request.getIssuedBy() != null) certification.setIssuedBy(request.getIssuedBy());
        if (request.getIssueDate() != null) certification.setIssueDate(request.getIssueDate());
        if (request.getExpiryDate() != null) certification.setExpiryDate(request.getExpiryDate());
        if (request.getCertificateUrl() != null) certification.setCertificateUrl(request.getCertificateUrl());
        if (request.getStatus() != null) certification.setStatus(request.getStatus());
        certification.setUpdatedBy(currentUser);
        return toResponse(certificationRepository.save(certification));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Certification certification = certificationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", "id", id));
        certification.setDeleted(true);
        certification.setUpdatedBy(currentUser);
        certificationRepository.save(certification);
    }

    @Transactional(readOnly = true)
    public List<CertificationDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return certificationRepository.findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CertificationDto.Response revoke(String tenantId, UUID id, String currentUser) {
        Certification certification = certificationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification", "id", id));
        certification.setStatus(Certification.CertificationStatus.REVOKED);
        certification.setUpdatedBy(currentUser);
        return toResponse(certificationRepository.save(certification));
    }

    private CertificationDto.Response toResponse(Certification certification) {
        return CertificationDto.Response.builder()
                .id(certification.getId())
                .tenantId(certification.getTenantId())
                .employeeId(certification.getEmployeeId())
                .courseId(certification.getCourseId())
                .certificateName(certification.getCertificateName())
                .issuedBy(certification.getIssuedBy())
                .issueDate(certification.getIssueDate())
                .expiryDate(certification.getExpiryDate())
                .certificateUrl(certification.getCertificateUrl())
                .status(certification.getStatus())
                .createdBy(certification.getCreatedBy())
                .updatedBy(certification.getUpdatedBy())
                .createdAt(certification.getCreatedAt())
                .updatedAt(certification.getUpdatedAt())
                .build();
    }
}
