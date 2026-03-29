package com.hrms.lms.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.lms.dto.AssessmentDto;
import com.hrms.lms.service.AssessmentService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<AssessmentDto.Response> create(@Valid @RequestBody AssessmentDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(assessmentService.create(tenantId, request, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<AssessmentDto.Response> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(assessmentService.getById(tenantId, id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<Page<AssessmentDto.Response>> list(@PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(assessmentService.list(tenantId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<AssessmentDto.Response> update(@PathVariable UUID id,
                                                      @Valid @RequestBody AssessmentDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(assessmentService.update(tenantId, id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        assessmentService.delete(tenantId, id, currentUser);
        return ApiResponse.ok(null);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<List<AssessmentDto.Response>> listByCourse(@PathVariable UUID courseId) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(assessmentService.listByCourse(tenantId, courseId));
    }
}
