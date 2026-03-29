package com.hrms.payroll.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.payroll.dto.SalaryStructureDto;
import com.hrms.payroll.entity.SalaryComponent;
import com.hrms.payroll.entity.SalaryStructure;
import com.hrms.payroll.entity.SalaryStructureComponent;
import com.hrms.payroll.repository.SalaryComponentRepository;
import com.hrms.payroll.repository.SalaryStructureComponentRepository;
import com.hrms.payroll.repository.SalaryStructureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryStructureService {

    private final SalaryStructureRepository          structureRepository;
    private final SalaryStructureComponentRepository structureComponentRepository;
    private final SalaryComponentRepository          componentRepository;

    // ── Structure CRUD ────────────────────────────────────────────────────────

    @Transactional
    public SalaryStructureDto.Response create(String tenantId, SalaryStructureDto.CreateRequest req,
                                               String currentUser) {
        if (structureRepository.existsByNameAndTenantIdAndDeletedFalse(req.getName(), tenantId)) {
            throw new DuplicateResourceException("SalaryStructure", "name", req.getName());
        }
        SalaryStructure structure = new SalaryStructure();
        structure.setTenantId(tenantId);
        structure.setName(req.getName());
        structure.setDescription(req.getDescription());
        structure.setDefaultStructure(req.isDefaultStructure());
        structure.setCreatedBy(currentUser);

        // If this is set as default, clear existing default first
        if (req.isDefaultStructure()) {
            clearDefaultFlag(tenantId, currentUser);
        }
        return toResponse(structureRepository.save(structure), List.of());
    }

    @Transactional
    public SalaryStructureDto.Response update(String tenantId, UUID id,
                                               SalaryStructureDto.UpdateRequest req,
                                               String currentUser) {
        SalaryStructure structure = getEntity(tenantId, id);
        if (req.getName() != null) {
            if (structureRepository.existsByNameAndTenantIdAndDeletedFalseAndIdNot(req.getName(), tenantId, id)) {
                throw new DuplicateResourceException("SalaryStructure", "name", req.getName());
            }
            structure.setName(req.getName());
        }
        if (req.getDescription() != null)       structure.setDescription(req.getDescription());
        if (req.getActive() != null)            structure.setActive(req.getActive());
        if (req.getDefaultStructure() != null) {
            if (req.getDefaultStructure()) clearDefaultFlag(tenantId, currentUser);
            structure.setDefaultStructure(req.getDefaultStructure());
        }
        structure.setUpdatedBy(currentUser);
        SalaryStructure saved = structureRepository.save(structure);
        List<SalaryStructureComponent> comps = structureComponentRepository.findBySalaryStructureId(id);
        return toResponse(saved, comps);
    }

    @Transactional(readOnly = true)
    public SalaryStructureDto.Response getById(String tenantId, UUID id) {
        SalaryStructure structure = getEntity(tenantId, id);
        List<SalaryStructureComponent> comps = structureComponentRepository.findBySalaryStructureId(id);
        return toResponse(structure, comps);
    }

    @Transactional(readOnly = true)
    public Page<SalaryStructureDto.Response> list(String tenantId, Pageable pageable) {
        return structureRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(s -> {
                    List<SalaryStructureComponent> comps = structureComponentRepository
                            .findBySalaryStructureId(s.getId());
                    return toResponse(s, comps);
                });
    }

    @Transactional(readOnly = true)
    public List<SalaryStructureDto.Response> listActive(String tenantId) {
        return structureRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(s -> toResponse(s, List.of())).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        SalaryStructure structure = getEntity(tenantId, id);
        structureComponentRepository.deleteAllByStructureId(id);
        structure.setDeleted(true);
        structure.setUpdatedBy(currentUser);
        structureRepository.save(structure);
    }

    // ── Component management within a structure ───────────────────────────────

    @Transactional
    public SalaryStructureDto.Response addComponent(String tenantId, UUID structureId,
                                                     SalaryStructureDto.ComponentAssignRequest req,
                                                     String currentUser) {
        getEntity(tenantId, structureId); // existence check
        UUID componentId = UUID.fromString(req.getComponentId());
        componentRepository.findByIdAndTenantIdAndDeletedFalse(componentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryComponent", "id", componentId));

        SalaryStructureComponent ssc = structureComponentRepository
                .findBySalaryStructureIdAndComponentId(structureId, componentId)
                .orElseGet(SalaryStructureComponent::new);
        ssc.setSalaryStructureId(structureId);
        ssc.setComponentId(componentId);
        ssc.setDefaultAmount(req.getDefaultAmount());
        ssc.setDefaultPercentage(req.getDefaultPercentage());
        ssc.setMandatory(req.isMandatory());
        structureComponentRepository.save(ssc);

        List<SalaryStructureComponent> comps = structureComponentRepository.findBySalaryStructureId(structureId);
        return toResponse(getEntity(tenantId, structureId), comps);
    }

    @Transactional
    public void removeComponent(String tenantId, UUID structureId, UUID componentId) {
        getEntity(tenantId, structureId);
        if (!structureComponentRepository.existsBySalaryStructureIdAndComponentId(structureId, componentId)) {
            throw new ResourceNotFoundException("SalaryStructureComponent", "componentId", componentId);
        }
        structureComponentRepository.deleteByStructureAndComponent(structureId, componentId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public SalaryStructure getEntity(String tenantId, UUID id) {
        return structureRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    private void clearDefaultFlag(String tenantId, String currentUser) {
        structureRepository.findFirstByTenantIdAndDefaultStructureTrueAndDeletedFalse(tenantId)
                .ifPresent(existing -> {
                    existing.setDefaultStructure(false);
                    existing.setUpdatedBy(currentUser);
                    structureRepository.save(existing);
                });
    }

    private SalaryStructureDto.Response toResponse(SalaryStructure s,
                                                    List<SalaryStructureComponent> comps) {
        // Build a map of component metadata for name/code/type enrichment
        List<UUID> compIds = comps.stream().map(SalaryStructureComponent::getComponentId)
                .collect(Collectors.toList());
        Map<UUID, SalaryComponent> compMap = compIds.isEmpty() ? Map.of()
                : componentRepository.findAllByIdInAndTenantId(compIds, s.getTenantId())
                        .stream().collect(Collectors.toMap(SalaryComponent::getId, c -> c));

        List<SalaryStructureDto.ComponentEntry> entries = comps.stream().map(ssc -> {
            SalaryComponent c = compMap.get(ssc.getComponentId());
            return SalaryStructureDto.ComponentEntry.builder()
                    .id(ssc.getId())
                    .componentId(ssc.getComponentId())
                    .componentName(c != null ? c.getName() : null)
                    .componentCode(c != null ? c.getCode() : null)
                    .componentType(c != null ? c.getType().name() : null)
                    .defaultAmount(ssc.getDefaultAmount())
                    .defaultPercentage(ssc.getDefaultPercentage())
                    .mandatory(ssc.isMandatory())
                    .build();
        }).collect(Collectors.toList());

        return SalaryStructureDto.Response.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .defaultStructure(s.isDefaultStructure())
                .active(s.isActive())
                .components(entries)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
