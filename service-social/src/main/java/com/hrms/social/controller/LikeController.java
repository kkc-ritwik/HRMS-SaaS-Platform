package com.hrms.social.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import com.hrms.social.dto.LikeDto;
import com.hrms.social.entity.Like;
import com.hrms.social.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<LikeDto.Response>> toggleLike(
            @Valid @RequestBody LikeDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        LikeDto.Response response = likeService.toggleLike(
                tenantId,
                request.getEmployeeId(),
                request.getPostId(),
                request.getReactionType() != null ? request.getReactionType() : Like.ReactionType.LIKE,
                currentUser);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(response));
    }

    @GetMapping("/post/{postId}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<List<LikeDto.Response>>> listByPost(@PathVariable UUID postId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(likeService.listByPost(tenantId, postId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        likeService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
