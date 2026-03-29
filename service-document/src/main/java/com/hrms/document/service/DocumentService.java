package com.hrms.document.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.document.dto.DocumentDto;
import com.hrms.document.entity.Document;
import com.hrms.document.entity.Document.DocumentStatus;
import com.hrms.document.repository.DocumentRepository;
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
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentDto.Response create(String tenantId, DocumentDto.CreateRequest req, String userId) {
        Document entity = new Document();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setFileName(req.getFileName());
        entity.setEmployeeId(req.getEmployeeId());
        entity.setDocumentTypeId(req.getDocumentTypeId());
        entity.setFileUrl(req.getFileUrl());
        entity.setFileSizeBytes(req.getFileSizeBytes());
        entity.setMimeType(req.getMimeType());
        entity.setExpiryDate(req.getExpiryDate());
        entity.setRemarks(req.getRemarks());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : DocumentStatus.DRAFT);
        entity.setUploadedBy(userId != null ? UUID.fromString(userId) : null);
        return toResponse(documentRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<DocumentDto.Response> list(String tenantId, Pageable pageable) {
        return documentRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DocumentDto.Response> listAll(String tenantId) {
        return documentRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<DocumentDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return documentRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DocumentDto.Response update(String tenantId, UUID id, DocumentDto.UpdateRequest req, String userId) {
        Document entity = findOrThrow(tenantId, id);
        if (req.getFileName() != null) entity.setFileName(req.getFileName());
        if (req.getEmployeeId() != null) entity.setEmployeeId(req.getEmployeeId());
        if (req.getDocumentTypeId() != null) entity.setDocumentTypeId(req.getDocumentTypeId());
        if (req.getFileUrl() != null) entity.setFileUrl(req.getFileUrl());
        if (req.getFileSizeBytes() != null) entity.setFileSizeBytes(req.getFileSizeBytes());
        if (req.getMimeType() != null) entity.setMimeType(req.getMimeType());
        if (req.getExpiryDate() != null) entity.setExpiryDate(req.getExpiryDate());
        if (req.getRemarks() != null) entity.setRemarks(req.getRemarks());
        if (req.getStatus() != null) entity.setStatus(req.getStatus());
        entity.setUpdatedBy(userId);
        return toResponse(documentRepository.save(entity));
    }

    public void delete(String tenantId, UUID id, String userId) {
        Document entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(userId);
        documentRepository.save(entity);
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

    private Document findOrThrow(String tenantId, UUID id) {
        return documentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
    }

    private DocumentDto.Response toResponse(Document e) {
        return DocumentDto.Response.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .documentTypeId(e.getDocumentTypeId())
                .fileName(e.getFileName())
                .fileUrl(e.getFileUrl())
                .fileSizeBytes(e.getFileSizeBytes())
                .mimeType(e.getMimeType())
                .status(e.getStatus())
                .expiryDate(e.getExpiryDate())
                .remarks(e.getRemarks())
                .uploadedBy(e.getUploadedBy())
                .tenantId(e.getTenantId())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
