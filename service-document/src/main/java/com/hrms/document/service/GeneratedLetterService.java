package com.hrms.document.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.document.dto.GeneratedLetterDto;
import com.hrms.document.entity.GeneratedLetter;
import com.hrms.document.entity.GeneratedLetter.LetterStatus;
import com.hrms.document.repository.GeneratedLetterRepository;
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
public class GeneratedLetterService {

    private final GeneratedLetterRepository generatedLetterRepository;

    public GeneratedLetterDto.Response create(String tenantId, GeneratedLetterDto.CreateRequest req, String userId) {
        GeneratedLetter entity = new GeneratedLetter();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setEmployeeId(req.getEmployeeId());
        entity.setTemplateId(req.getTemplateId());
        entity.setLetterType(req.getLetterType());
        entity.setSubject(req.getSubject());
        entity.setContent(req.getContent());
        entity.setGeneratedBy(userId != null ? UUID.fromString(userId) : null);
        entity.setStatus(LetterStatus.DRAFT);
        return toResponse(generatedLetterRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<GeneratedLetterDto.Response> list(String tenantId, Pageable pageable) {
        return generatedLetterRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<GeneratedLetterDto.Response> listAll(String tenantId) {
        return generatedLetterRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GeneratedLetterDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<GeneratedLetterDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return generatedLetterRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public GeneratedLetterDto.Response update(String tenantId, UUID id, GeneratedLetterDto.UpdateRequest req, String userId) {
        GeneratedLetter entity = findOrThrow(tenantId, id);
        if (req.getEmployeeId() != null) entity.setEmployeeId(req.getEmployeeId());
        if (req.getTemplateId() != null) entity.setTemplateId(req.getTemplateId());
        if (req.getLetterType() != null) entity.setLetterType(req.getLetterType());
        if (req.getSubject() != null) entity.setSubject(req.getSubject());
        if (req.getContent() != null) entity.setContent(req.getContent());
        if (req.getStatus() != null) entity.setStatus(req.getStatus());
        if (req.getSentAt() != null) entity.setSentAt(req.getSentAt());
        entity.setUpdatedBy(userId);
        return toResponse(generatedLetterRepository.save(entity));
    }

    public void delete(String tenantId, UUID id, String userId) {
        GeneratedLetter entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(userId);
        generatedLetterRepository.save(entity);
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

    private GeneratedLetter findOrThrow(String tenantId, UUID id) {
        return generatedLetterRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("GeneratedLetter", "id", id));
    }

    private GeneratedLetterDto.Response toResponse(GeneratedLetter e) {
        return GeneratedLetterDto.Response.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .templateId(e.getTemplateId())
                .letterType(e.getLetterType())
                .subject(e.getSubject())
                .content(e.getContent())
                .generatedBy(e.getGeneratedBy())
                .sentAt(e.getSentAt())
                .status(e.getStatus())
                .tenantId(e.getTenantId())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
