package com.hrms.lms.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.lms.dto.AssessmentDto;
import com.hrms.lms.entity.Assessment;
import com.hrms.lms.repository.AssessmentRepository;
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
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;

    @Transactional
    public AssessmentDto.Response create(String tenantId, AssessmentDto.CreateRequest request, String currentUser) {
        Assessment assessment = new Assessment();
        assessment.setTenantId(tenantId);
        assessment.setCreatedBy(currentUser);
        assessment.setUpdatedBy(currentUser);
        assessment.setCourseId(request.getCourseId());
        assessment.setTitle(request.getTitle());
        assessment.setDescription(request.getDescription());
        assessment.setTotalMarks(request.getTotalMarks() > 0 ? request.getTotalMarks() : 100);
        assessment.setPassingMarks(request.getPassingMarks() > 0 ? request.getPassingMarks() : 60);
        assessment.setDurationMinutes(request.getDurationMinutes());
        assessment.setAttemptsAllowed(request.getAttemptsAllowed() > 0 ? request.getAttemptsAllowed() : 3);
        assessment.setStatus(Assessment.AssessmentStatus.DRAFT);
        assessment.setQuestions(request.getQuestions() != null ? request.getQuestions() : List.of());
        return toResponse(assessmentRepository.save(assessment));
    }

    @Transactional(readOnly = true)
    public AssessmentDto.Response getById(String tenantId, UUID id) {
        Assessment assessment = assessmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));
        return toResponse(assessment);
    }

    @Transactional(readOnly = true)
    public Page<AssessmentDto.Response> list(String tenantId, Pageable pageable) {
        return assessmentRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssessmentDto.Response> listAll(String tenantId) {
        return assessmentRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public AssessmentDto.Response update(String tenantId, UUID id, AssessmentDto.UpdateRequest request, String currentUser) {
        Assessment assessment = assessmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));
        if (request.getCourseId() != null) assessment.setCourseId(request.getCourseId());
        if (request.getTitle() != null) assessment.setTitle(request.getTitle());
        if (request.getDescription() != null) assessment.setDescription(request.getDescription());
        if (request.getTotalMarks() != null) assessment.setTotalMarks(request.getTotalMarks());
        if (request.getPassingMarks() != null) assessment.setPassingMarks(request.getPassingMarks());
        if (request.getDurationMinutes() != null) assessment.setDurationMinutes(request.getDurationMinutes());
        if (request.getAttemptsAllowed() != null) assessment.setAttemptsAllowed(request.getAttemptsAllowed());
        if (request.getStatus() != null) assessment.setStatus(request.getStatus());
        if (request.getQuestions() != null) assessment.setQuestions(request.getQuestions());
        assessment.setUpdatedBy(currentUser);
        return toResponse(assessmentRepository.save(assessment));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Assessment assessment = assessmentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", "id", id));
        assessment.setDeleted(true);
        assessment.setUpdatedBy(currentUser);
        assessmentRepository.save(assessment);
    }

    @Transactional(readOnly = true)
    public List<AssessmentDto.Response> listByCourse(String tenantId, UUID courseId) {
        return assessmentRepository.findByTenantIdAndCourseIdAndDeletedFalse(tenantId, courseId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private AssessmentDto.Response toResponse(Assessment assessment) {
        return AssessmentDto.Response.builder()
                .id(assessment.getId())
                .tenantId(assessment.getTenantId())
                .courseId(assessment.getCourseId())
                .title(assessment.getTitle())
                .description(assessment.getDescription())
                .totalMarks(assessment.getTotalMarks())
                .passingMarks(assessment.getPassingMarks())
                .durationMinutes(assessment.getDurationMinutes())
                .attemptsAllowed(assessment.getAttemptsAllowed())
                .status(assessment.getStatus())
                .questions(assessment.getQuestions())
                .createdBy(assessment.getCreatedBy())
                .updatedBy(assessment.getUpdatedBy())
                .createdAt(assessment.getCreatedAt())
                .updatedAt(assessment.getUpdatedAt())
                .build();
    }
}
