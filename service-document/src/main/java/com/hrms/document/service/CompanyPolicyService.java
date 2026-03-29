package com.hrms.document.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.document.dto.CompanyPolicyDto;
import com.hrms.document.entity.CompanyPolicy;
import com.hrms.document.entity.CompanyPolicy.PolicyStatus;
import com.hrms.document.repository.CompanyPolicyRepository;
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
@Transactional
public class CompanyPolicyService {

    private final CompanyPolicyRepository companyPolicyRepository;

    public CompanyPolicyDto.Response create(String tenantId, CompanyPolicyDto.CreateRequest req, String userId) {
        CompanyPolicy entity = new CompanyPolicy();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setTitle(req.getTitle());
        entity.setDescription(req.getDescription());
        entity.setPolicyType(req.getPolicyType());
        entity.setContent(req.getContent());
        entity.setVersion(req.getVersion());
        entity.setEffectiveDate(req.getEffectiveDate());
        entity.setExpiryDate(req.getExpiryDate());
        entity.setRequiresAcknowledgement(req.isRequiresAcknowledgement());
        entity.setStatus(PolicyStatus.DRAFT);
        return toResponse(companyPolicyRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CompanyPolicyDto.Response> list(String tenantId, Pageable pageable) {
        return companyPolicyRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CompanyPolicyDto.Response> listAll(String tenantId) {
        return companyPolicyRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompanyPolicyDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    public CompanyPolicyDto.Response update(String tenantId, UUID id, CompanyPolicyDto.UpdateRequest req, String userId) {
        CompanyPolicy entity = findOrThrow(tenantId, id);
        if (req.getTitle() != null) entity.setTitle(req.getTitle());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getPolicyType() != null) entity.setPolicyType(req.getPolicyType());
        if (req.getContent() != null) entity.setContent(req.getContent());
        if (req.getVersion() != null) entity.setVersion(req.getVersion());
        if (req.getEffectiveDate() != null) entity.setEffectiveDate(req.getEffectiveDate());
        if (req.getExpiryDate() != null) entity.setExpiryDate(req.getExpiryDate());
        if (req.getRequiresAcknowledgement() != null) entity.setRequiresAcknowledgement(req.getRequiresAcknowledgement());
        if (req.getStatus() != null) entity.setStatus(req.getStatus());
        entity.setUpdatedBy(userId);
        return toResponse(companyPolicyRepository.save(entity));
    }

    public void delete(String tenantId, UUID id, String userId) {
        CompanyPolicy entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(userId);
        companyPolicyRepository.save(entity);
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

    private CompanyPolicy findOrThrow(String tenantId, UUID id) {
        return companyPolicyRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyPolicy", "id", id));
    }

    private CompanyPolicyDto.Response toResponse(CompanyPolicy e) {
        return CompanyPolicyDto.Response.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .policyType(e.getPolicyType())
                .content(e.getContent())
                .version(e.getVersion())
                .effectiveDate(e.getEffectiveDate())
                .expiryDate(e.getExpiryDate())
                .status(e.getStatus())
                .requiresAcknowledgement(e.isRequiresAcknowledgement())
                .tenantId(e.getTenantId())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
