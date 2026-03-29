package com.hrms.lms.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.lms.dto.CourseModuleDto;
import com.hrms.lms.entity.CourseModule;
import com.hrms.lms.repository.CourseModuleRepository;
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
public class CourseModuleService {

    private final CourseModuleRepository courseModuleRepository;

    @Transactional
    public CourseModuleDto.Response create(String tenantId, CourseModuleDto.CreateRequest request, String currentUser) {
        CourseModule module = new CourseModule();
        module.setTenantId(tenantId);
        module.setCreatedBy(currentUser);
        module.setUpdatedBy(currentUser);
        module.setCourseId(request.getCourseId());
        module.setTitle(request.getTitle());
        module.setDescription(request.getDescription());
        module.setOrderIndex(request.getOrderIndex());
        module.setDurationMinutes(request.getDurationMinutes());
        module.setContentType(request.getContentType() != null ? request.getContentType() : CourseModule.ContentType.DOCUMENT);
        module.setContentUrl(request.getContentUrl());
        module.setStatus(CourseModule.ModuleStatus.DRAFT);
        return toResponse(courseModuleRepository.save(module));
    }

    @Transactional(readOnly = true)
    public CourseModuleDto.Response getById(String tenantId, UUID id) {
        CourseModule module = courseModuleRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseModule", "id", id));
        return toResponse(module);
    }

    @Transactional(readOnly = true)
    public Page<CourseModuleDto.Response> list(String tenantId, Pageable pageable) {
        return courseModuleRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CourseModuleDto.Response> listAll(String tenantId) {
        return courseModuleRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CourseModuleDto.Response update(String tenantId, UUID id, CourseModuleDto.UpdateRequest request, String currentUser) {
        CourseModule module = courseModuleRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseModule", "id", id));
        if (request.getCourseId() != null) module.setCourseId(request.getCourseId());
        if (request.getTitle() != null) module.setTitle(request.getTitle());
        if (request.getDescription() != null) module.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) module.setOrderIndex(request.getOrderIndex());
        if (request.getDurationMinutes() != null) module.setDurationMinutes(request.getDurationMinutes());
        if (request.getContentType() != null) module.setContentType(request.getContentType());
        if (request.getContentUrl() != null) module.setContentUrl(request.getContentUrl());
        if (request.getStatus() != null) module.setStatus(request.getStatus());
        module.setUpdatedBy(currentUser);
        return toResponse(courseModuleRepository.save(module));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        CourseModule module = courseModuleRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseModule", "id", id));
        module.setDeleted(true);
        module.setUpdatedBy(currentUser);
        courseModuleRepository.save(module);
    }

    @Transactional(readOnly = true)
    public List<CourseModuleDto.Response> listByCourse(String tenantId, UUID courseId) {
        return courseModuleRepository.findByTenantIdAndCourseIdAndDeletedFalseOrderByOrderIndexAsc(tenantId, courseId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private CourseModuleDto.Response toResponse(CourseModule module) {
        return CourseModuleDto.Response.builder()
                .id(module.getId())
                .tenantId(module.getTenantId())
                .courseId(module.getCourseId())
                .title(module.getTitle())
                .description(module.getDescription())
                .orderIndex(module.getOrderIndex())
                .durationMinutes(module.getDurationMinutes())
                .contentType(module.getContentType())
                .contentUrl(module.getContentUrl())
                .status(module.getStatus())
                .createdBy(module.getCreatedBy())
                .updatedBy(module.getUpdatedBy())
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }
}
