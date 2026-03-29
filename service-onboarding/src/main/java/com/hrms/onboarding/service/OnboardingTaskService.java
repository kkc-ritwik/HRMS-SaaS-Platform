package com.hrms.onboarding.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.onboarding.dto.OnboardingTaskDto;
import com.hrms.onboarding.entity.OnboardingTask;
import com.hrms.onboarding.entity.OnboardingTask.TaskType;
import com.hrms.onboarding.entity.OnboardingTask.TaskStatus;
import com.hrms.onboarding.repository.OnboardingTaskRepository;
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
public class OnboardingTaskService {

    private final OnboardingTaskRepository onboardingTaskRepository;

    @Transactional
    public OnboardingTaskDto.Response create(String tenantId, OnboardingTaskDto.CreateRequest req, String currentUser) {
        OnboardingTask task = new OnboardingTask();
        task.setTenantId(tenantId);
        task.setTemplateId(req.getTemplateId());
        task.setEmployeeId(req.getEmployeeId());
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setTaskType(req.getTaskType() != null ? req.getTaskType() : TaskType.OTHER);
        task.setAssignedToRole(req.getAssignedToRole());
        task.setDueDaysOffset(req.getDueDaysOffset());
        task.setRequired(req.isRequired());
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedBy(currentUser);
        task.setUpdatedBy(currentUser);
        return toResponse(onboardingTaskRepository.save(task));
    }

    @Transactional
    public OnboardingTaskDto.Response update(String tenantId, UUID id, OnboardingTaskDto.UpdateRequest req, String currentUser) {
        OnboardingTask task = getEntity(tenantId, id);
        if (req.getTemplateId() != null) {
            task.setTemplateId(req.getTemplateId());
        }
        if (req.getEmployeeId() != null) {
            task.setEmployeeId(req.getEmployeeId());
        }
        if (req.getTitle() != null) {
            task.setTitle(req.getTitle());
        }
        if (req.getDescription() != null) {
            task.setDescription(req.getDescription());
        }
        if (req.getTaskType() != null) {
            task.setTaskType(req.getTaskType());
        }
        if (req.getAssignedToRole() != null) {
            task.setAssignedToRole(req.getAssignedToRole());
        }
        if (req.getDueDaysOffset() != null) {
            task.setDueDaysOffset(req.getDueDaysOffset());
        }
        if (req.getRequired() != null) {
            task.setRequired(req.getRequired());
        }
        if (req.getStatus() != null) {
            task.setStatus(req.getStatus());
        }
        if (req.getCompletedAt() != null) {
            task.setCompletedAt(req.getCompletedAt());
        }
        task.setUpdatedBy(currentUser);
        return toResponse(onboardingTaskRepository.save(task));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        OnboardingTask task = getEntity(tenantId, id);
        task.setDeleted(true);
        task.setUpdatedBy(currentUser);
        onboardingTaskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public OnboardingTaskDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<OnboardingTaskDto.Response> list(String tenantId, Pageable pageable) {
        return onboardingTaskRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OnboardingTaskDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return onboardingTaskRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OnboardingTaskDto.Response> listByTemplate(String tenantId, UUID templateId) {
        return onboardingTaskRepository
                .findByTenantIdAndTemplateIdAndDeletedFalse(tenantId, templateId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

    private OnboardingTask getEntity(String tenantId, UUID id) {
        return onboardingTaskRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("OnboardingTask", "id", id));
    }

    private OnboardingTaskDto.Response toResponse(OnboardingTask task) {
        return OnboardingTaskDto.Response.builder()
                .id(task.getId())
                .tenantId(task.getTenantId())
                .templateId(task.getTemplateId())
                .employeeId(task.getEmployeeId())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .assignedToRole(task.getAssignedToRole())
                .dueDaysOffset(task.getDueDaysOffset())
                .required(task.isRequired())
                .status(task.getStatus())
                .completedAt(task.getCompletedAt())
                .createdBy(task.getCreatedBy())
                .updatedBy(task.getUpdatedBy())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
