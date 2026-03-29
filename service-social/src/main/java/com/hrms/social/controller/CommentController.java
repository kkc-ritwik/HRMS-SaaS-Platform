package com.hrms.social.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import com.hrms.social.dto.CommentDto;
import com.hrms.social.service.CommentService;
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
@RequestMapping("/api/v1/posts/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<CommentDto.Response>> create(
            @Valid @RequestBody CommentDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        CommentDto.Response response = commentService.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<CommentDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(commentService.getById(tenantId, id)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<Page<CommentDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(commentService.list(tenantId, pageable)));
    }

    @GetMapping("/post/{postId}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<List<CommentDto.Response>>> listByPost(@PathVariable UUID postId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(commentService.listByPost(tenantId, postId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<CommentDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CommentDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(commentService.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        commentService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
