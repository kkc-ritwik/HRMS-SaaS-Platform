package com.hrms.asset.service;

import com.hrms.asset.dto.AssetRequestDto;
import com.hrms.asset.entity.AssetRequest;
import com.hrms.asset.entity.AssetRequest.RequestStatus;
import com.hrms.asset.repository.AssetRequestRepository;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetRequestService {

    private final AssetRequestRepository assetRequestRepository;

    @Transactional
    public AssetRequestDto.Response create(String tenantId, AssetRequestDto.CreateRequest req,
                                            String currentUser) {
        AssetRequest request = new AssetRequest();
        request.setTenantId(tenantId);
        request.setEmployeeId(req.getEmployeeId());
        request.setCategoryId(req.getCategoryId());
        request.setAssetId(req.getAssetId());
        request.setReason(req.getReason());
        request.setRequiredFrom(req.getRequiredFrom());
        request.setRequiredUntil(req.getRequiredUntil());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedBy(currentUser);
        request.setUpdatedBy(currentUser);
        return toResponse(assetRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public Page<AssetRequestDto.Response> list(String tenantId, Pageable pageable) {
        return assetRequestRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssetRequestDto.Response> listAll(String tenantId) {
        return assetRequestRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssetRequestDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<AssetRequestDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return assetRequestRepository.findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AssetRequestDto.Response update(String tenantId, UUID id,
                                            AssetRequestDto.UpdateRequest req, String currentUser) {
        AssetRequest request = findOrThrow(tenantId, id);
        if (req.getStatus() != null)     request.setStatus(req.getStatus());
        if (req.getApprovedBy() != null) request.setApprovedBy(req.getApprovedBy());
        if (req.getNotes() != null)      request.setNotes(req.getNotes());
        request.setUpdatedBy(currentUser);
        return toResponse(assetRequestRepository.save(request));
    }

    @Transactional
    public AssetRequestDto.Response approve(String tenantId, UUID id, UUID approvedBy,
                                             String currentUser) {
        AssetRequest request = findOrThrow(tenantId, id);
        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setUpdatedBy(currentUser);
        return toResponse(assetRequestRepository.save(request));
    }

    @Transactional
    public AssetRequestDto.Response reject(String tenantId, UUID id, String currentUser) {
        AssetRequest request = findOrThrow(tenantId, id);
        request.setStatus(RequestStatus.REJECTED);
        request.setUpdatedBy(currentUser);
        return toResponse(assetRequestRepository.save(request));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        AssetRequest request = findOrThrow(tenantId, id);
        request.setDeleted(true);
        request.setUpdatedBy(currentUser);
        assetRequestRepository.save(request);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize()).total(page.getTotalElements())
                .totalPages(page.getTotalPages()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private AssetRequest findOrThrow(String tenantId, UUID id) {
        return assetRequestRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AssetRequest", "id", id));
    }

    AssetRequestDto.Response toResponse(AssetRequest r) {
        return AssetRequestDto.Response.builder()
                .id(r.getId())
                .tenantId(r.getTenantId())
                .employeeId(r.getEmployeeId())
                .categoryId(r.getCategoryId())
                .assetId(r.getAssetId())
                .reason(r.getReason())
                .requiredFrom(r.getRequiredFrom())
                .requiredUntil(r.getRequiredUntil())
                .status(r.getStatus())
                .approvedBy(r.getApprovedBy())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
