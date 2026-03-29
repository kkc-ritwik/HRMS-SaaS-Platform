package com.hrms.lms.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.lms.dto.CourseEnrollmentDto;
import com.hrms.lms.service.CourseEnrollmentService;
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

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses/enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService courseEnrollmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CourseEnrollmentDto.Response> create(@Valid @RequestBody CourseEnrollmentDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(courseEnrollmentService.create(tenantId, request, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<CourseEnrollmentDto.Response> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseEnrollmentService.getById(tenantId, id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<Page<CourseEnrollmentDto.Response>> list(@PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseEnrollmentService.list(tenantId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CourseEnrollmentDto.Response> update(@PathVariable UUID id,
                                                            @Valid @RequestBody CourseEnrollmentDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(courseEnrollmentService.update(tenantId, id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        courseEnrollmentService.delete(tenantId, id, currentUser);
        return ApiResponse.ok(null);
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<Page<CourseEnrollmentDto.Response>> listByEmployee(@PathVariable UUID employeeId,
                                                                          @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseEnrollmentService.listByEmployee(tenantId, employeeId, pageable));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<Page<CourseEnrollmentDto.Response>> listByCourse(@PathVariable UUID courseId,
                                                                        @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseEnrollmentService.listByCourse(tenantId, courseId, pageable));
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CourseEnrollmentDto.Response> updateProgress(@PathVariable UUID id,
                                                                    @RequestBody Map<String, BigDecimal> body) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        BigDecimal progressPercentage = body.get("progressPercentage");
        return ApiResponse.ok(courseEnrollmentService.updateProgress(tenantId, id, progressPercentage, currentUser));
    }
}
