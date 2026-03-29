package com.hrms.document.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.document.dto.DocumentTypeDto;
import com.hrms.document.entity.DocumentType;
import com.hrms.document.repository.DocumentTypeRepository;
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
public class DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;

    public DocumentTypeDto.Response create(String tenantId, DocumentTypeDto.CreateRequest req, String userId) {
        DocumentType entity = new DocumentType();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setDescription(req.getDescription());
        entity.setRequiredForOnboarding(req.isRequiredForOnboarding());
        entity.setActive(req.isActive());
        return toResponse(documentTypeRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<DocumentTypeDto.Response> list(String tenantId, Pageable pageable) {
        return documentTypeRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DocumentTypeDto.Response> listAll(String tenantId) {
        return documentTypeRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentTypeDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    public DocumentTypeDto.Response update(String tenantId, UUID id, DocumentTypeDto.UpdateRequest req, String userId) {
        DocumentType entity = findOrThrow(tenantId, id);
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getCode() != null) entity.setCode(req.getCode());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getRequiredForOnboarding() != null) entity.setRequiredForOnboarding(req.getRequiredForOnboarding());
        if (req.getActive() != null) entity.setActive(req.getActive());
        entity.setUpdatedBy(userId);
        return toResponse(documentTypeRepository.save(entity));
    }

    public void delete(String tenantId, UUID id, String userId) {
        DocumentType entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(userId);
        documentTypeRepository.save(entity);
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

    private DocumentType findOrThrow(String tenantId, UUID id) {
        return documentTypeRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentType", "id", id));
    }

    private DocumentTypeDto.Response toResponse(DocumentType e) {
        return DocumentTypeDto.Response.builder()
                .id(e.getId())
                .name(e.getName())
                .code(e.getCode())
                .description(e.getDescription())
                .requiredForOnboarding(e.isRequiredForOnboarding())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
