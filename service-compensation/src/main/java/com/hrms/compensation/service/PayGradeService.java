package com.hrms.compensation.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.compensation.dto.PayGradeDto;
import com.hrms.compensation.entity.PayGrade;
import com.hrms.compensation.repository.PayGradeRepository;
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
public class PayGradeService {

    private final PayGradeRepository payGradeRepository;

    @Transactional
    public PayGradeDto.Response create(String tenantId, PayGradeDto.CreateRequest request, String currentUser) {
        PayGrade entity = new PayGrade();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setMinSalary(request.getMinSalary());
        entity.setMaxSalary(request.getMaxSalary());
        entity.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        entity.setLevel(request.getLevel());
        entity.setActive(request.isActive());
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(payGradeRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PayGradeDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<PayGradeDto.Response> list(String tenantId, Pageable pageable) {
        return payGradeRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PayGradeDto.Response> listAll(String tenantId) {
        return payGradeRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PayGradeDto.Response> listActive(String tenantId) {
        return payGradeRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public PayGradeDto.Response update(String tenantId, UUID id, PayGradeDto.UpdateRequest request, String currentUser) {
        PayGrade entity = findOrThrow(tenantId, id);
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getCode() != null) entity.setCode(request.getCode());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getMinSalary() != null) entity.setMinSalary(request.getMinSalary());
        if (request.getMaxSalary() != null) entity.setMaxSalary(request.getMaxSalary());
        if (request.getCurrency() != null) entity.setCurrency(request.getCurrency());
        if (request.getLevel() != null) entity.setLevel(request.getLevel());
        if (request.getActive() != null) entity.setActive(request.getActive());
        entity.setUpdatedBy(currentUser);
        return toResponse(payGradeRepository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        PayGrade entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        payGradeRepository.save(entity);
    }

    private PayGrade findOrThrow(String tenantId, UUID id) {
        return payGradeRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PayGrade", "id", id));
    }

    private PayGradeDto.Response toResponse(PayGrade e) {
        return PayGradeDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .name(e.getName())
                .code(e.getCode())
                .description(e.getDescription())
                .minSalary(e.getMinSalary())
                .maxSalary(e.getMaxSalary())
                .currency(e.getCurrency())
                .level(e.getLevel())
                .active(e.isActive())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
