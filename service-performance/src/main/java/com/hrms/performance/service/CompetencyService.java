package com.hrms.performance.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.performance.dto.CompetencyDto;
import com.hrms.performance.entity.Competency;
import com.hrms.performance.entity.RoleCompetency;
import com.hrms.performance.repository.CompetencyRepository;
import com.hrms.performance.repository.RoleCompetencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompetencyService {

    private final CompetencyRepository     competencyRepository;
    private final RoleCompetencyRepository roleCompetencyRepository;

    // ── Competency CRUD ───────────────────────────────────────────────────────

    @Transactional
    public CompetencyDto.Response create(String tenantId, CompetencyDto.CreateRequest req,
                                          String currentUser) {
        if (competencyRepository.existsByTenantIdAndNameAndDeletedFalse(tenantId, req.getName())) {
            throw new DuplicateResourceException("Competency", "name", req.getName());
        }
        Competency c = new Competency();
        c.setTenantId(tenantId);
        c.setName(req.getName());
        c.setDescription(req.getDescription());
        c.setCategory(req.getCategory());
        c.setBehaviors(req.getBehaviors());
        c.setActive(true);
        c.setCreatedBy(currentUser);
        return toResponse(competencyRepository.save(c));
    }

    @Transactional
    public CompetencyDto.Response update(String tenantId, UUID id,
                                          CompetencyDto.UpdateRequest req, String currentUser) {
        Competency c = getEntity(tenantId, id);
        if (req.getName() != null)        c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getCategory() != null)    c.setCategory(req.getCategory());
        if (req.getBehaviors() != null)   c.setBehaviors(req.getBehaviors());
        if (req.getActive() != null)      c.setActive(req.getActive());
        c.setUpdatedBy(currentUser);
        return toResponse(competencyRepository.save(c));
    }

    @Transactional(readOnly = true)
    public CompetencyDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<CompetencyDto.Response> list(String tenantId, Pageable pageable) {
        return competencyRepository.findByTenantIdAndDeletedFalseOrderByNameAsc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CompetencyDto.Response> listActive(String tenantId) {
        return competencyRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(this::toResponse).toList();
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── Role mappings ─────────────────────────────────────────────────────────

    @Transactional
    public CompetencyDto.RoleMappingResponse addRoleMapping(String tenantId,
                                                             CompetencyDto.RoleMappingRequest req,
                                                             String currentUser) {
        getEntity(tenantId, req.getCompetencyId()); // verify competency exists
        if (req.getRoleId() != null &&
            roleCompetencyRepository.existsByTenantIdAndCompetencyIdAndRoleIdAndDeletedFalse(
                    tenantId, req.getCompetencyId(), req.getRoleId())) {
            throw new BusinessException("DUPLICATE_MAPPING",
                    "This competency is already mapped to the given role");
        }
        RoleCompetency rc = new RoleCompetency();
        rc.setTenantId(tenantId);
        rc.setCompetencyId(req.getCompetencyId());
        rc.setRoleId(req.getRoleId());
        rc.setDepartmentId(req.getDepartmentId());
        rc.setExpectedLevel(req.getExpectedLevel());
        rc.setWeightage(req.getWeightage());
        rc.setCreatedBy(currentUser);
        return toMappingResponse(roleCompetencyRepository.save(rc), null);
    }

    @Transactional
    public void removeRoleMapping(String tenantId, UUID mappingId, String currentUser) {
        RoleCompetency rc = roleCompetencyRepository.findByIdAndTenantIdAndDeletedFalse(mappingId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RoleCompetency", "id", mappingId));
        rc.setDeleted(true);
        rc.setUpdatedBy(currentUser);
        roleCompetencyRepository.save(rc);
    }

    @Transactional(readOnly = true)
    public List<CompetencyDto.RoleMappingResponse> getMappingsForRole(String tenantId, UUID roleId) {
        return roleCompetencyRepository.findByTenantIdAndRoleIdAndDeletedFalse(tenantId, roleId)
                .stream().map(rc -> toMappingResponse(rc, resolveCompetencyName(tenantId, rc.getCompetencyId())))
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    Competency getEntity(String tenantId, UUID id) {
        return competencyRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Competency", "id", id));
    }

    private String resolveCompetencyName(String tenantId, UUID competencyId) {
        return competencyRepository.findByIdAndTenantIdAndDeletedFalse(competencyId, tenantId)
                .map(Competency::getName).orElse(null);
    }

    CompetencyDto.Response toResponse(Competency c) {
        return CompetencyDto.Response.builder()
                .id(c.getId()).name(c.getName()).description(c.getDescription())
                .category(c.getCategory()).behaviors(c.getBehaviors()).active(c.isActive())
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt())
                .build();
    }

    private CompetencyDto.RoleMappingResponse toMappingResponse(RoleCompetency rc, String competencyName) {
        return CompetencyDto.RoleMappingResponse.builder()
                .id(rc.getId()).competencyId(rc.getCompetencyId())
                .competencyName(competencyName)
                .roleId(rc.getRoleId()).departmentId(rc.getDepartmentId())
                .expectedLevel(rc.getExpectedLevel()).weightage(rc.getWeightage())
                .createdAt(rc.getCreatedAt())
                .build();
    }
}
