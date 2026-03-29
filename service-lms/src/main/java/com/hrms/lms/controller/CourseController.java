package com.hrms.lms.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.lms.dto.CourseDto;
import com.hrms.lms.entity.Course;
import com.hrms.lms.service.CourseService;
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
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CourseDto.Response> create(@Valid @RequestBody CourseDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(courseService.create(tenantId, request, currentUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<CourseDto.Response> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseService.getById(tenantId, id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<Page<CourseDto.Response>> list(@PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseService.list(tenantId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CourseDto.Response> update(@PathVariable UUID id,
                                                  @Valid @RequestBody CourseDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(courseService.update(tenantId, id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        courseService.delete(tenantId, id, currentUser);
        return ApiResponse.ok(null);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<Page<CourseDto.Response>> listByStatus(@PathVariable Course.CourseStatus status,
                                                              @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseService.listByStatus(tenantId, status, pageable));
    }

    @GetMapping("/mandatory")
    @PreAuthorize("hasAuthority('LMS:READ')")
    public ApiResponse<List<CourseDto.Response>> listMandatory() {
        String tenantId = TenantContext.get();
        return ApiResponse.ok(courseService.listMandatory(tenantId));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CourseDto.Response> publish(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(courseService.publish(tenantId, id, currentUser));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('LMS:WRITE')")
    public ApiResponse<CourseDto.Response> archive(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return ApiResponse.ok(courseService.archive(tenantId, id, currentUser));
    }
}
