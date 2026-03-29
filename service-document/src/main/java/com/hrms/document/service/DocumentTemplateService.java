package com.hrms.document.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.document.dto.DocumentTemplateDto;
import com.hrms.document.entity.DocumentTemplate;
import com.hrms.document.repository.DocumentTemplateRepository;
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
public class DocumentTemplateService {

    private final DocumentTemplateRepository documentTemplateRepository;

    public DocumentTemplateDto.Response create(String tenantId, DocumentTemplateDto.CreateRequest req, String userId) {
        DocumentTemplate entity = new DocumentTemplate();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setDescription(req.getDescription());
        entity.setContent(req.getContent());
        entity.setCategory(req.getCategory());
        entity.setActive(req.isActive());
        return toResponse(documentTemplateRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<DocumentTemplateDto.Response> list(String tenantId, Pageable pageable) {
        return documentTemplateRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DocumentTemplateDto.Response> listAll(String tenantId) {
        return documentTemplateRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentTemplateDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    public DocumentTemplateDto.Response update(String tenantId, UUID id, DocumentTemplateDto.UpdateRequest req, String userId) {
        DocumentTemplate entity = findOrThrow(tenantId, id);
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getCode() != null) entity.setCode(req.getCode());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());
        if (req.getContent() != null) entity.setContent(req.getContent());
        if (req.getCategory() != null) entity.setCategory(req.getCategory());
        if (req.getActive() != null) entity.setActive(req.getActive());
        entity.setUpdatedBy(userId);
        return toResponse(documentTemplateRepository.save(entity));
    }

    public void delete(String tenantId, UUID id, String userId) {
        DocumentTemplate entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(userId);
        documentTemplateRepository.save(entity);
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

    private DocumentTemplate findOrThrow(String tenantId, UUID id) {
        return documentTemplateRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentTemplate", "id", id));
    }

    private DocumentTemplateDto.Response toResponse(DocumentTemplate e) {
        return DocumentTemplateDto.Response.builder()
                .id(e.getId())
                .name(e.getName())
                .code(e.getCode())
                .description(e.getDescription())
                .content(e.getContent())
                .category(e.getCategory())
                .active(e.isActive())
                .tenantId(e.getTenantId())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
