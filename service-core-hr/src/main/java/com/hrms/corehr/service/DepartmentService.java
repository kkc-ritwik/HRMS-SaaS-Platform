package com.hrms.corehr.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.corehr.dto.DepartmentDto;
import com.hrms.corehr.entity.Department;
import com.hrms.corehr.mapper.DepartmentMapper;
import com.hrms.corehr.repository.DepartmentRepository;
import com.hrms.corehr.repository.EmployeeRepository;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository   employeeRepository;
    private final DepartmentMapper     mapper;

    @Transactional
    public DepartmentDto.Response create(String tenantId, DepartmentDto.CreateRequest req,
                                         String currentUser) {
        if (departmentRepository.existsByCodeAndTenantIdAndDeletedFalse(req.getCode(), tenantId)) {
            throw new DuplicateResourceException("Department", "code", req.getCode());
        }
        Department dept = mapper.toEntity(req);
        dept.setTenantId(tenantId);
        dept.setCreatedBy(currentUser);
        dept.setUpdatedBy(currentUser);
        Department saved = departmentRepository.save(dept);
        return enrich(saved);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentDto.Response> list(String tenantId, String search, Pageable pageable) {
        Page<Department> page = (search == null || search.isBlank())
                ? departmentRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                : departmentRepository.searchByTenant(tenantId, search, pageable);
        return page.map(this::enrich);
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
    public DepartmentDto.Response getById(String tenantId, UUID id) {
        Department dept = findOrThrow(tenantId, id);
        DepartmentDto.Response response = enrich(dept);
        // attach children
        List<DepartmentDto.Response> children = departmentRepository
                .findByParentIdAndTenantIdAndDeletedFalse(id, tenantId)
                .stream().map(this::enrich).toList();
        response.setChildren(children);
        return response;
    }

    @Transactional
    public DepartmentDto.Response update(String tenantId, UUID id,
                                          DepartmentDto.UpdateRequest req, String currentUser) {
        Department dept = findOrThrow(tenantId, id);
        mapper.updateEntity(req, dept);
        dept.setUpdatedBy(currentUser);
        return enrich(departmentRepository.save(dept));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Department dept = findOrThrow(tenantId, id);
        dept.setDeleted(true);
        dept.setUpdatedBy(currentUser);
        departmentRepository.save(dept);
        log.info("[CoreHR] Department {} soft-deleted by {}", id, currentUser);
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto.ListItem> listActive(String tenantId) {
        return departmentRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(mapper::toListItem).toList();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private Department findOrThrow(String tenantId, UUID id) {
        return departmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    private DepartmentDto.Response enrich(Department dept) {
        DepartmentDto.Response r = mapper.toResponse(dept);
        // headCount from DB-computed column or live count
        long count = employeeRepository.countByDepartmentIdAndTenantIdAndDeletedFalse(
                dept.getId(), dept.getTenantId());
        r.setHeadCount((int) count);
        if (dept.getParentId() != null) {
            departmentRepository.findById(dept.getParentId())
                    .ifPresent(p -> r.setParentName(p.getName()));
        }
        return r;
    }
}
