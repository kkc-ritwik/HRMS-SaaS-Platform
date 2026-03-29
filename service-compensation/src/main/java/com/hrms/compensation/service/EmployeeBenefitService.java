package com.hrms.compensation.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.compensation.dto.EmployeeBenefitDto;
import com.hrms.compensation.entity.EmployeeBenefit;
import com.hrms.compensation.entity.EmployeeBenefit.BenefitStatus;
import com.hrms.compensation.repository.EmployeeBenefitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeBenefitService {

    private final EmployeeBenefitRepository employeeBenefitRepository;

    @Transactional
    public EmployeeBenefitDto.Response create(String tenantId, EmployeeBenefitDto.CreateRequest request, String currentUser) {
        EmployeeBenefit entity = new EmployeeBenefit();
        entity.setTenantId(tenantId);
        entity.setEmployeeId(request.getEmployeeId());
        entity.setBenefitId(request.getBenefitId());
        entity.setEnrollmentDate(request.getEnrollmentDate());
        entity.setStatus(BenefitStatus.ACTIVE);
        entity.setNotes(request.getNotes());
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(employeeBenefitRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public EmployeeBenefitDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeBenefitDto.Response> list(String tenantId, Pageable pageable) {
        return employeeBenefitRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<EmployeeBenefitDto.Response> listAll(String tenantId) {
        return employeeBenefitRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeBenefitDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return employeeBenefitRepository.findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public EmployeeBenefitDto.Response update(String tenantId, UUID id, EmployeeBenefitDto.UpdateRequest request, String currentUser) {
        EmployeeBenefit entity = findOrThrow(tenantId, id);
        if (request.getTerminationDate() != null) entity.setTerminationDate(request.getTerminationDate());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        entity.setUpdatedBy(currentUser);
        return toResponse(employeeBenefitRepository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        EmployeeBenefit entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        employeeBenefitRepository.save(entity);
    }

    @Transactional
    public EmployeeBenefitDto.Response terminate(String tenantId, UUID id, LocalDate terminationDate, String currentUser) {
        EmployeeBenefit entity = findOrThrow(tenantId, id);
        entity.setStatus(BenefitStatus.TERMINATED);
        entity.setTerminationDate(terminationDate != null ? terminationDate : LocalDate.now());
        entity.setUpdatedBy(currentUser);
        return toResponse(employeeBenefitRepository.save(entity));
    }

    private EmployeeBenefit findOrThrow(String tenantId, UUID id) {
        return employeeBenefitRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeBenefit", "id", id));
    }

    private EmployeeBenefitDto.Response toResponse(EmployeeBenefit e) {
        return EmployeeBenefitDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .employeeId(e.getEmployeeId())
                .benefitId(e.getBenefitId())
                .enrollmentDate(e.getEnrollmentDate())
                .terminationDate(e.getTerminationDate())
                .status(e.getStatus())
                .notes(e.getNotes())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
