package com.hrms.lms.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.lms.dto.CourseModuleDto;
import com.hrms.lms.service.CourseModuleService;
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
@RequestMapping("/api/v1/courses/modules")
@RequiredArgsConstructor
public class CourseModuleController {

    private final CourseModuleService courseModuleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CourseModuleDto.Response> create(@Valid @RequestBody CourseModuleDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(courseModuleService.create(tenantId, request, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<CourseModuleDto.Response> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseModuleService.getById(tenantId, id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<Page<CourseModuleDto.Response>> list(@PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseModuleService.list(tenantId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CourseModuleDto.Response> update(@PathVariable UUID id,
                                                        @Valid @RequestBody CourseModuleDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(courseModuleService.update(tenantId, id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        courseModuleService.delete(tenantId, id, currentUser);
        return ApiResponse.ok(null);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<List<CourseModuleDto.Response>> listByCourse(@PathVariable UUID courseId) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseModuleService.listByCourse(tenantId, courseId));
    }
}
