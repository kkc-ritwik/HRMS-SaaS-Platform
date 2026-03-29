package com.hrms.social.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import com.hrms.social.dto.GroupMemberDto;
import com.hrms.social.entity.GroupMember;
import com.hrms.social.service.GroupMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/members")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @PostMapping
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<GroupMemberDto.Response>> create(
            @Valid @RequestBody GroupMemberDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        GroupMemberDto.Response response = groupMemberService.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<GroupMemberDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(groupMemberService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<Page<GroupMemberDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(groupMemberService.list(tenantId, pageable)));
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<List<GroupMemberDto.Response>>> listByGroup(@PathVariable UUID groupId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(groupMemberService.listByGroup(tenantId, groupId)));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<List<GroupMemberDto.Response>>> listByEmployee(@PathVariable UUID employeeId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(groupMemberService.listByEmployee(tenantId, employeeId)));
    }

    @PostMapping("/join")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<GroupMemberDto.Response>> join(
            @RequestParam UUID groupId,
            @RequestParam UUID employeeId,
            @RequestParam(required = false) GroupMember.MemberRole role) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        GroupMemberDto.Response response = groupMemberService.addMember(tenantId, groupId, employeeId, role, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @DeleteMapping("/leave/{groupId}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<Void>> leave(
            @PathVariable UUID groupId,
            @RequestParam UUID employeeId) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        groupMemberService.removeMember(tenantId, groupId, employeeId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<GroupMemberDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody GroupMemberDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(groupMemberService.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        groupMemberService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
