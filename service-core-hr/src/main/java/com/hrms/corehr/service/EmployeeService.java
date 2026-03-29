package com.hrms.corehr.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.corehr.dto.EmployeeDto;
import com.hrms.corehr.dto.OrgChartNode;
import com.hrms.corehr.entity.*;
import com.hrms.corehr.mapper.EmployeeMapper;
import com.hrms.corehr.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository               employeeRepository;
    private final AddressRepository                addressRepository;
    private final EmployeeLifecycleEventRepository lifecycleEventRepository;
    private final DepartmentRepository             departmentRepository;
    private final DesignationRepository            designationRepository;
    private final LocationRepository               locationRepository;
    private final EmployeeMapper                   mapper;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional
    public EmployeeDto.Response create(String tenantId, EmployeeDto.CreateRequest req,
                                        String currentUser) {
        if (employeeRepository.existsByEmailAndTenantIdAndDeletedFalse(req.getEmail(), tenantId)) {
            throw new DuplicateResourceException("Employee", "email", req.getEmail());
        }
        Employee employee = mapper.toEntity(req);
        employee.setTenantId(tenantId);
        employee.setEmployeeCode(generateEmployeeCode(tenantId));
        employee.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        employee.setDisplayName(buildDisplayName(req.getFirstName(), req.getMiddleName(), req.getLastName()));
        employee.setCreatedBy(currentUser);
        employee.setUpdatedBy(currentUser);
        Employee saved = employeeRepository.save(employee);

        // Record JOIN lifecycle event
        recordLifecycleEvent(saved, EmployeeLifecycleEvent.EventType.JOINED,
                null, "ACTIVE", "Employee onboarded", currentUser, tenantId);

        return enrich(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeDto.Response getById(String tenantId, UUID id) {
        return enrich(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto.ListItem> list(String tenantId, String search,
                                            UUID departmentId, UUID locationId,
                                            Employee.EmploymentStatus status,
                                            Pageable pageable) {
        Page<Employee> page = (search == null && departmentId == null
                && locationId == null && status == null)
                ? employeeRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                : employeeRepository.filterByTenant(tenantId, departmentId, locationId,
                                                    status, search, pageable);
        return page.map(this::toListItem);
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

    @Transactional
    public EmployeeDto.Response update(String tenantId, UUID id,
                                        EmployeeDto.UpdateRequest req, String currentUser) {
        Employee employee = findOrThrow(tenantId, id);

        // Track changes for lifecycle events
        trackAndRecordChanges(employee, req, currentUser, tenantId);

        mapper.updateEntity(req, employee);
        // Recompute displayName if any name field changed
        String fn = req.getFirstName()  != null ? req.getFirstName()  : employee.getFirstName();
        String mn = req.getMiddleName() != null ? req.getMiddleName() : employee.getMiddleName();
        String ln = req.getLastName()   != null ? req.getLastName()   : employee.getLastName();
        employee.setDisplayName(buildDisplayName(fn, mn, ln));
        employee.setUpdatedBy(currentUser);
        return enrich(employeeRepository.save(employee));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Employee employee = findOrThrow(tenantId, id);
        employee.setDeleted(true);
        employee.setUpdatedBy(currentUser);
        employeeRepository.save(employee);
        log.info("[CoreHR] Employee {} soft-deleted by {}", id, currentUser);
    }

    // ── Directory ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<EmployeeDto.DirectoryItem> directory(String tenantId, String search,
                                                      Pageable pageable) {
        Page<Employee> page = (search == null || search.isBlank())
                ? employeeRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                : employeeRepository.searchByTenant(tenantId, search, pageable);
        return page.map(this::toDirectoryItem);
    }

    // ── Team members ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmployeeDto.ListItem> getTeamMembers(String tenantId, UUID managerId) {
        findOrThrow(tenantId, managerId); // validate manager exists
        return employeeRepository.findByManagerIdAndTenantIdAndDeletedFalse(managerId, tenantId)
                .stream().map(this::toListItem).toList();
    }

    // ── Lifecycle events ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmployeeDto.LifecycleEventResponse> getLifecycleEvents(String tenantId, UUID id) {
        findOrThrow(tenantId, id); // validate employee exists
        return lifecycleEventRepository
                .findByEmployeeIdAndDeletedFalseOrderByEventDateDesc(id)
                .stream().map(mapper::toLifecycleResponse).toList();
    }

    // ── Manual lifecycle events ───────────────────────────────────────────────

    @Transactional
    public void addLifecycleEvent(String tenantId, UUID employeeId,
                                   EmployeeDto.LifecycleEvent req, String currentUser) {
        findOrThrow(tenantId, employeeId); // validates employee + tenant

        EmployeeLifecycleEvent.EventType type;
        try {
            type = EmployeeLifecycleEvent.EventType.valueOf(req.getEventType().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_EVENT_TYPE",
                    "Unknown event type: " + req.getEventType() +
                    ". Valid values: " + Arrays.toString(EmployeeLifecycleEvent.EventType.values()));
        }

        EmployeeLifecycleEvent event = new EmployeeLifecycleEvent();
        event.setTenantId(tenantId);
        event.setEmployeeId(employeeId);
        event.setEventType(type);
        event.setEventDate(req.getEffectiveDate());
        event.setEffectiveDate(req.getEffectiveDate());
        event.setOldValue(req.getOldValueJson());
        event.setNewValue(req.getNewValueJson());
        event.setReason(req.getReason());
        event.setComments(req.getComments());
        event.setRemarks(req.getComments());
        event.setPerformedBy(currentUser);
        event.setCreatedBy(currentUser);
        event.setUpdatedBy(currentUser);
        lifecycleEventRepository.save(event);
        log.info("[CoreHR] Lifecycle event {} recorded for employee {} by {}", type, employeeId, currentUser);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto.LifecycleEventResponse> getTimeline(String tenantId, UUID employeeId) {
        findOrThrow(tenantId, employeeId);
        return lifecycleEventRepository
                .findByEmployeeIdAndDeletedFalseOrderByEventDateDesc(employeeId)
                .stream().map(mapper::toLifecycleResponse).toList();
    }

    // ── Org chart ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OrgChartNode> buildOrgChart(String tenantId) {
        List<Employee> all = employeeRepository.findAllByTenantIdAndDeletedFalse(tenantId);
        Map<UUID, List<Employee>> byManager = all.stream()
                .filter(e -> e.getManagerId() != null)
                .collect(Collectors.groupingBy(Employee::getManagerId));

        // Root nodes: employees with no manager, or whose manager is not in the tenant
        Set<UUID> allIds = all.stream().map(Employee::getId).collect(Collectors.toSet());
        return all.stream()
                .filter(e -> e.getManagerId() == null || !allIds.contains(e.getManagerId()))
                .<OrgChartNode>map(e -> buildNode(e, byManager, tenantId))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrgChartNode buildOrgChartFrom(String tenantId, UUID employeeId) {
        Employee root = findOrThrow(tenantId, employeeId);
        List<Employee> all = employeeRepository.findAllByTenantIdAndDeletedFalse(tenantId);
        Map<UUID, List<Employee>> byManager = all.stream()
                .filter(e -> e.getManagerId() != null)
                .collect(Collectors.groupingBy(Employee::getManagerId));
        return buildNode(root, byManager, tenantId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Employee findOrThrow(String tenantId, UUID id) {
        return employeeRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    private String generateEmployeeCode(String tenantId) {
        long count = employeeRepository.countByTenantIdAndDeletedFalse(tenantId);
        String code;
        do {
            count++;
            code = String.format("EMP%05d", count);
        } while (employeeRepository.existsByEmployeeCodeAndTenantIdAndDeletedFalse(code, tenantId));
        return code;
    }

    private EmployeeDto.Response enrich(Employee emp) {
        EmployeeDto.Response r = mapper.toResponse(emp);

        if (emp.getDepartmentId() != null) {
            departmentRepository.findById(emp.getDepartmentId())
                    .ifPresent(d -> r.setDepartmentName(d.getName()));
        }
        if (emp.getDesignationId() != null) {
            designationRepository.findById(emp.getDesignationId())
                    .ifPresent(d -> r.setDesignationName(d.getName()));
        }
        if (emp.getLocationId() != null) {
            locationRepository.findById(emp.getLocationId())
                    .ifPresent(l -> r.setLocationName(l.getName()));
        }
        if (emp.getManagerId() != null) {
            employeeRepository.findById(emp.getManagerId()).ifPresent(m ->
                    r.setManagerName(m.getFirstName() + " " + m.getLastName()));
        }
        List<EmployeeDto.AddressInfo> addresses = addressRepository
                .findByEmployeeIdAndDeletedFalse(emp.getId())
                .stream().map(mapper::toAddressInfo).toList();
        r.setAddresses(addresses);
        return r;
    }

    private EmployeeDto.ListItem toListItem(Employee emp) {
        EmployeeDto.ListItem item = mapper.toListItem(emp);
        if (emp.getDepartmentId() != null) {
            departmentRepository.findById(emp.getDepartmentId())
                    .ifPresent(d -> item.setDepartmentName(d.getName()));
        }
        if (emp.getDesignationId() != null) {
            designationRepository.findById(emp.getDesignationId())
                    .ifPresent(d -> item.setDesignationName(d.getName()));
        }
        if (emp.getLocationId() != null) {
            locationRepository.findById(emp.getLocationId())
                    .ifPresent(l -> item.setLocationName(l.getName()));
        }
        return item;
    }

    private EmployeeDto.DirectoryItem toDirectoryItem(Employee emp) {
        EmployeeDto.DirectoryItem item = mapper.toDirectoryItem(emp);
        item.setEmailWork(emp.getWorkEmail() != null ? emp.getWorkEmail() : emp.getEmail());
        item.setPhonePrimary(emp.getPhone());
        item.setPhotoUrl(emp.getProfilePictureUrl());
        if (emp.getDepartmentId() != null) {
            departmentRepository.findById(emp.getDepartmentId())
                    .ifPresent(d -> item.setDepartmentName(d.getName()));
        }
        if (emp.getDesignationId() != null) {
            designationRepository.findById(emp.getDesignationId())
                    .ifPresent(d -> item.setDesignationName(d.getName()));
        }
        if (emp.getLocationId() != null) {
            locationRepository.findById(emp.getLocationId())
                    .ifPresent(l -> item.setLocationName(l.getName()));
        }
        return item;
    }

    private OrgChartNode buildNode(Employee emp, Map<UUID, List<Employee>> byManager, String tenantId) {
        String designationName = null;
        if (emp.getDesignationId() != null) {
            designationName = designationRepository.findById(emp.getDesignationId())
                    .map(d -> d.getName()).orElse(null);
        }
        String departmentName = null;
        if (emp.getDepartmentId() != null) {
            departmentName = departmentRepository.findById(emp.getDepartmentId())
                    .map(d -> d.getName()).orElse(null);
        }
        List<OrgChartNode> children = byManager.getOrDefault(emp.getId(), List.of())
                .stream()
                .<OrgChartNode>map(child -> buildNode(child, byManager, tenantId))
                .toList();
        return OrgChartNode.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .displayName(emp.getDisplayName() != null ? emp.getDisplayName()
                        : emp.getFirstName() + " " + emp.getLastName())
                .designation(designationName)
                .department(departmentName)
                .photoUrl(emp.getProfilePictureUrl())
                .email(emp.getWorkEmail() != null ? emp.getWorkEmail() : emp.getEmail())
                .children(children)
                .build();
    }

    private static String buildDisplayName(String firstName, String middleName, String lastName) {
        StringBuilder sb = new StringBuilder(firstName != null ? firstName : "");
        if (middleName != null && !middleName.isBlank()) sb.append(" ").append(middleName);
        if (lastName  != null && !lastName.isBlank())   sb.append(" ").append(lastName);
        return sb.toString().trim();
    }

    private void trackAndRecordChanges(Employee existing, EmployeeDto.UpdateRequest req,
                                        String currentUser, String tenantId) {
        if (req.getDepartmentId() != null
                && !req.getDepartmentId().equals(existing.getDepartmentId())) {
            recordLifecycleEvent(existing, EmployeeLifecycleEvent.EventType.TRANSFERRED,
                    existing.getDepartmentId() != null ? existing.getDepartmentId().toString() : null,
                    req.getDepartmentId().toString(),
                    "Department changed", currentUser, tenantId);
        }
        if (req.getDesignationId() != null
                && !req.getDesignationId().equals(existing.getDesignationId())) {
            recordLifecycleEvent(existing, EmployeeLifecycleEvent.EventType.PROMOTED,
                    existing.getDesignationId() != null ? existing.getDesignationId().toString() : null,
                    req.getDesignationId().toString(),
                    "Designation changed", currentUser, tenantId);
        }
        if (req.getEmploymentStatus() != null
                && req.getEmploymentStatus() != existing.getEmploymentStatus()) {
            EmployeeLifecycleEvent.EventType evtType = switch (req.getEmploymentStatus()) {
                case RESIGNED    -> EmployeeLifecycleEvent.EventType.RESIGNED;
                case TERMINATED  -> EmployeeLifecycleEvent.EventType.TERMINATED;
                default          -> EmployeeLifecycleEvent.EventType.STATUS_CHANGED;
            };
            recordLifecycleEvent(existing, evtType,
                    existing.getEmploymentStatus().name(),
                    req.getEmploymentStatus().name(),
                    "Status changed", currentUser, tenantId);
        }
    }

    private void recordLifecycleEvent(Employee employee, EmployeeLifecycleEvent.EventType type,
                                       String oldValue, String newValue,
                                       String remarks, String performedBy, String tenantId) {
        EmployeeLifecycleEvent event = new EmployeeLifecycleEvent();
        event.setTenantId(tenantId);
        event.setEmployeeId(employee.getId());
        event.setEventType(type);
        event.setEventDate(LocalDate.now());
        event.setOldValue(oldValue);
        event.setNewValue(newValue);
        event.setRemarks(remarks);
        event.setPerformedBy(performedBy);
        event.setCreatedBy(performedBy);
        event.setUpdatedBy(performedBy);
        lifecycleEventRepository.save(event);
    }
}
