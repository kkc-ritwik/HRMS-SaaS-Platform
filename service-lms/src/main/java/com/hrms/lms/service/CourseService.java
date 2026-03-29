package com.hrms.lms.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.lms.dto.CourseDto;
import com.hrms.lms.entity.Course;
import com.hrms.lms.repository.CourseRepository;
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
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional
    public CourseDto.Response create(String tenantId, CourseDto.CreateRequest request, String currentUser) {
        Course course = new Course();
        course.setTenantId(tenantId);
        course.setCreatedBy(currentUser);
        course.setUpdatedBy(currentUser);
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(request.getCategory());
        course.setInstructorId(request.getInstructorId());
        course.setDurationHours(request.getDurationHours());
        course.setLevel(request.getLevel() != null ? request.getLevel() : Course.CourseLevel.BEGINNER);
        course.setFormat(request.getFormat() != null ? request.getFormat() : Course.CourseFormat.ONLINE);
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setStatus(Course.CourseStatus.DRAFT);
        course.setTags(request.getTags() != null ? request.getTags() : List.of());
        course.setMandatory(request.isMandatory());
        course.setTargetRoles(request.getTargetRoles() != null ? request.getTargetRoles() : List.of());
        return toResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public CourseDto.Response getById(String tenantId, UUID id) {
        Course course = courseRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return toResponse(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseDto.Response> list(String tenantId, Pageable pageable) {
        return courseRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CourseDto.Response> listAll(String tenantId) {
        return courseRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CourseDto.Response update(String tenantId, UUID id, CourseDto.UpdateRequest request, String currentUser) {
        Course course = courseRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getCategory() != null) course.setCategory(request.getCategory());
        if (request.getInstructorId() != null) course.setInstructorId(request.getInstructorId());
        if (request.getDurationHours() != null) course.setDurationHours(request.getDurationHours());
        if (request.getLevel() != null) course.setLevel(request.getLevel());
        if (request.getFormat() != null) course.setFormat(request.getFormat());
        if (request.getThumbnailUrl() != null) course.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getStatus() != null) course.setStatus(request.getStatus());
        if (request.getTags() != null) course.setTags(request.getTags());
        if (request.getMandatory() != null) course.setMandatory(request.getMandatory());
        if (request.getTargetRoles() != null) course.setTargetRoles(request.getTargetRoles());
        course.setUpdatedBy(currentUser);
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Course course = courseRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        course.setDeleted(true);
        course.setUpdatedBy(currentUser);
        courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseDto.Response> listByStatus(String tenantId, Course.CourseStatus status, Pageable pageable) {
        return courseRepository.findByTenantIdAndStatusAndDeletedFalse(tenantId, status, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CourseDto.Response> listMandatory(String tenantId) {
        return courseRepository.findByTenantIdAndMandatoryAndDeletedFalse(tenantId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CourseDto.Response publish(String tenantId, UUID id, String currentUser) {
        Course course = courseRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        course.setStatus(Course.CourseStatus.PUBLISHED);
        course.setUpdatedBy(currentUser);
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseDto.Response archive(String tenantId, UUID id, String currentUser) {
        Course course = courseRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        course.setStatus(Course.CourseStatus.ARCHIVED);
        course.setUpdatedBy(currentUser);
        return toResponse(courseRepository.save(course));
    }

    private CourseDto.Response toResponse(Course course) {
        return CourseDto.Response.builder()
                .id(course.getId())
                .tenantId(course.getTenantId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .instructorId(course.getInstructorId())
                .durationHours(course.getDurationHours())
                .level(course.getLevel())
                .format(course.getFormat())
                .thumbnailUrl(course.getThumbnailUrl())
                .status(course.getStatus())
                .tags(course.getTags())
                .mandatory(course.isMandatory())
                .targetRoles(course.getTargetRoles())
                .createdBy(course.getCreatedBy())
                .updatedBy(course.getUpdatedBy())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
