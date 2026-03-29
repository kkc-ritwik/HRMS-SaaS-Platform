package com.hrms.offboarding.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.offboarding.dto.KnowledgeTransferDto;
import com.hrms.offboarding.entity.KnowledgeTransfer;
import com.hrms.offboarding.entity.KnowledgeTransfer.TransferStatus;
import com.hrms.offboarding.repository.KnowledgeTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeTransferService {

    private final KnowledgeTransferRepository knowledgeTransferRepository;

    @Transactional
    public KnowledgeTransferDto.Response create(String tenantId, KnowledgeTransferDto.CreateRequest req, String currentUser) {
        KnowledgeTransfer kt = new KnowledgeTransfer();
        kt.setTenantId(tenantId);
        kt.setSeparationId(req.getSeparationId());
        kt.setFromEmployeeId(req.getFromEmployeeId());
        kt.setToEmployeeId(req.getToEmployeeId());
        kt.setTopic(req.getTopic());
        kt.setDescription(req.getDescription());
        kt.setDocumentUrl(req.getDocumentUrl());
        kt.setDueDate(req.getDueDate());
        kt.setStatus(TransferStatus.PENDING);
        kt.setCreatedBy(currentUser);
        kt.setUpdatedBy(currentUser);
        return toResponse(knowledgeTransferRepository.save(kt));
    }

    @Transactional(readOnly = true)
    public KnowledgeTransferDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeTransferDto.Response> list(String tenantId, Pageable pageable) {
        return knowledgeTransferRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public KnowledgeTransferDto.Response update(String tenantId, UUID id, KnowledgeTransferDto.UpdateRequest req, String currentUser) {
        KnowledgeTransfer kt = getEntity(tenantId, id);
        if (req.getTopic() != null) {
            kt.setTopic(req.getTopic());
        }
        if (req.getDescription() != null) {
            kt.setDescription(req.getDescription());
        }
        if (req.getDocumentUrl() != null) {
            kt.setDocumentUrl(req.getDocumentUrl());
        }
        if (req.getDueDate() != null) {
            kt.setDueDate(req.getDueDate());
        }
        if (req.getStatus() != null) {
            kt.setStatus(req.getStatus());
        }
        if (req.getCompletedAt() != null) {
            kt.setCompletedAt(req.getCompletedAt());
        }
        kt.setUpdatedBy(currentUser);
        return toResponse(knowledgeTransferRepository.save(kt));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        KnowledgeTransfer kt = getEntity(tenantId, id);
        kt.setDeleted(true);
        kt.setUpdatedBy(currentUser);
        knowledgeTransferRepository.save(kt);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeTransferDto.Response> listBySeparation(String tenantId, UUID separationId) {
        return knowledgeTransferRepository
                .findByTenantIdAndSeparationIdAndDeletedFalse(tenantId, separationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KnowledgeTransferDto.Response> listByFromEmployee(String tenantId, UUID employeeId) {
        return knowledgeTransferRepository
                .findByTenantIdAndFromEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KnowledgeTransferDto.Response> listByToEmployee(String tenantId, UUID employeeId) {
        return knowledgeTransferRepository
                .findByTenantIdAndToEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public KnowledgeTransferDto.Response complete(String tenantId, UUID id, String currentUser) {
        KnowledgeTransfer kt = getEntity(tenantId, id);
        kt.setStatus(TransferStatus.COMPLETED);
        kt.setCompletedAt(Instant.now());
        kt.setUpdatedBy(currentUser);
        return toResponse(knowledgeTransferRepository.save(kt));
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

    private KnowledgeTransfer getEntity(String tenantId, UUID id) {
        return knowledgeTransferRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("KnowledgeTransfer", "id", id));
    }

    private KnowledgeTransferDto.Response toResponse(KnowledgeTransfer kt) {
        return KnowledgeTransferDto.Response.builder()
                .id(kt.getId())
                .tenantId(kt.getTenantId())
                .separationId(kt.getSeparationId())
                .fromEmployeeId(kt.getFromEmployeeId())
                .toEmployeeId(kt.getToEmployeeId())
                .topic(kt.getTopic())
                .description(kt.getDescription())
                .documentUrl(kt.getDocumentUrl())
                .status(kt.getStatus())
                .dueDate(kt.getDueDate())
                .completedAt(kt.getCompletedAt())
                .createdBy(kt.getCreatedBy())
                .updatedBy(kt.getUpdatedBy())
                .createdAt(kt.getCreatedAt())
                .updatedAt(kt.getUpdatedAt())
                .build();
    }
}
