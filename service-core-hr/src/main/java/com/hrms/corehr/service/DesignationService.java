package com.hrms.corehr.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.corehr.dto.DesignationDto;
import com.hrms.corehr.entity.Designation;
import com.hrms.corehr.mapper.DesignationMapper;
import com.hrms.corehr.repository.DesignationRepository;
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
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final DesignationMapper     mapper;

    @Transactional
    public DesignationDto.Response create(String tenantId, DesignationDto.CreateRequest req,
                                           String currentUser) {
        if (designationRepository.existsByCodeAndTenantIdAndDeletedFalse(req.getCode(), tenantId)) {
            throw new DuplicateResourceException("Designation", "code", req.getCode());
        }
        Designation designation = mapper.toEntity(req);
        designation.setTenantId(tenantId);
        designation.setCreatedBy(currentUser);
        designation.setUpdatedBy(currentUser);
        return mapper.toResponse(designationRepository.save(designation));
    }

    @Transactional(readOnly = true)
    public Page<DesignationDto.Response> list(String tenantId, String search, Pageable pageable) {
        Page<Designation> page = (search == null || search.isBlank())
                ? designationRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                : designationRepository.searchByTenant(tenantId, search, pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public DesignationDto.Response getById(String tenantId, UUID id) {
        return mapper.toResponse(findOrThrow(tenantId, id));
    }

    @Transactional
    public DesignationDto.Response update(String tenantId, UUID id,
                                           DesignationDto.UpdateRequest req, String currentUser) {
        Designation designation = findOrThrow(tenantId, id);
        mapper.updateEntity(req, designation);
        designation.setUpdatedBy(currentUser);
        return mapper.toResponse(designationRepository.save(designation));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Designation designation = findOrThrow(tenantId, id);
        designation.setDeleted(true);
        designation.setUpdatedBy(currentUser);
        designationRepository.save(designation);
        log.info("[CoreHR] Designation {} soft-deleted by {}", id, currentUser);
    }

    @Transactional(readOnly = true)
    public List<DesignationDto.Response> listActive(String tenantId) {
        return designationRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(mapper::toResponse).toList();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private Designation findOrThrow(String tenantId, UUID id) {
        return designationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
    }
}
