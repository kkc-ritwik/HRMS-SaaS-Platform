package com.hrms.asset.service;

import com.hrms.asset.dto.AssetAssignmentDto;
import com.hrms.asset.entity.AssetAssignment;
import com.hrms.asset.entity.AssetAssignment.AssignmentStatus;
import com.hrms.asset.repository.AssetAssignmentRepository;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetAssignmentService {

    private final AssetAssignmentRepository assetAssignmentRepository;

    @Transactional
    public AssetAssignmentDto.Response create(String tenantId, AssetAssignmentDto.CreateRequest req,
                                               String currentUser) {
        AssetAssignment assignment = new AssetAssignment();
        assignment.setTenantId(tenantId);
        assignment.setAssetId(req.getAssetId());
        assignment.setEmployeeId(req.getEmployeeId());
        assignment.setAssignedBy(req.getAssignedBy());
        assignment.setAssignedDate(req.getAssignedDate());
        assignment.setExpectedReturnDate(req.getExpectedReturnDate());
        assignment.setConditionAtAssignment(req.getConditionAtAssignment());
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setCreatedBy(currentUser);
        assignment.setUpdatedBy(currentUser);
        return toResponse(assetAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public Page<AssetAssignmentDto.Response> list(String tenantId, Pageable pageable) {
        return assetAssignmentRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssetAssignmentDto.Response> listAll(String tenantId) {
        return assetAssignmentRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssetAssignmentDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<AssetAssignmentDto.Response> listByAsset(String tenantId, UUID assetId) {
        return assetAssignmentRepository.findByTenantIdAndAssetIdAndDeletedFalse(tenantId, assetId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssetAssignmentDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return assetAssignmentRepository.findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AssetAssignmentDto.Response update(String tenantId, UUID id,
                                               AssetAssignmentDto.UpdateRequest req,
                                               String currentUser) {
        AssetAssignment assignment = findOrThrow(tenantId, id);
        if (req.getActualReturnDate() != null) assignment.setActualReturnDate(req.getActualReturnDate());
        if (req.getConditionAtReturn() != null) assignment.setConditionAtReturn(req.getConditionAtReturn());
        if (req.getStatus() != null)            assignment.setStatus(req.getStatus());
        assignment.setUpdatedBy(currentUser);
        return toResponse(assetAssignmentRepository.save(assignment));
    }

    @Transactional
    public AssetAssignmentDto.Response returnAsset(String tenantId, UUID id,
                                                    String conditionAtReturn, String currentUser) {
        AssetAssignment assignment = findOrThrow(tenantId, id);
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setActualReturnDate(LocalDate.now());
        if (conditionAtReturn != null) assignment.setConditionAtReturn(conditionAtReturn);
        assignment.setUpdatedBy(currentUser);
        return toResponse(assetAssignmentRepository.save(assignment));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        AssetAssignment assignment = findOrThrow(tenantId, id);
        assignment.setDeleted(true);
        assignment.setUpdatedBy(currentUser);
        assetAssignmentRepository.save(assignment);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize()).total(page.getTotalElements())
                .totalPages(page.getTotalPages()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private AssetAssignment findOrThrow(String tenantId, UUID id) {
        return assetAssignmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AssetAssignment", "id", id));
    }

    AssetAssignmentDto.Response toResponse(AssetAssignment a) {
        return AssetAssignmentDto.Response.builder()
                .id(a.getId())
                .tenantId(a.getTenantId())
                .assetId(a.getAssetId())
                .employeeId(a.getEmployeeId())
                .assignedBy(a.getAssignedBy())
                .assignedDate(a.getAssignedDate())
                .expectedReturnDate(a.getExpectedReturnDate())
                .actualReturnDate(a.getActualReturnDate())
                .conditionAtAssignment(a.getConditionAtAssignment())
                .conditionAtReturn(a.getConditionAtReturn())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
