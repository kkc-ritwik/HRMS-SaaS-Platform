package com.hrms.social.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.social.dto.LikeDto;
import com.hrms.social.entity.Like;
import com.hrms.social.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeDto.Response create(String tenantId, LikeDto.CreateRequest request, String currentUser) {
        Like like = new Like();
        like.setTenantId(tenantId);
        like.setEmployeeId(request.getEmployeeId());
        like.setPostId(request.getPostId());
        like.setCommentId(request.getCommentId());
        like.setReactionType(request.getReactionType() != null ? request.getReactionType() : Like.ReactionType.LIKE);
        like.setCreatedBy(currentUser);
        like.setUpdatedBy(currentUser);
        return toResponse(likeRepository.save(like));
    }

    public LikeDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    public Page<LikeDto.Response> list(String tenantId, Pageable pageable) {
        return likeRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    public List<LikeDto.Response> listByPost(String tenantId, UUID postId) {
        return likeRepository.findByTenantIdAndPostIdAndDeletedFalse(tenantId, postId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public LikeDto.Response toggleLike(String tenantId, UUID employeeId, UUID postId,
                                       Like.ReactionType reactionType, String currentUser) {
        Optional<Like> existing = likeRepository.findByTenantIdAndPostIdAndEmployeeIdAndDeletedFalse(tenantId, postId, employeeId);
        if (existing.isPresent()) {
            Like like = existing.get();
            like.setDeleted(true);
            like.setUpdatedBy(currentUser);
            likeRepository.save(like);
            return toResponse(like);
        } else {
            Like like = new Like();
            like.setTenantId(tenantId);
            like.setEmployeeId(employeeId);
            like.setPostId(postId);
            like.setReactionType(reactionType != null ? reactionType : Like.ReactionType.LIKE);
            like.setCreatedBy(currentUser);
            like.setUpdatedBy(currentUser);
            return toResponse(likeRepository.save(like));
        }
    }

    public LikeDto.Response update(String tenantId, UUID id, LikeDto.UpdateRequest request, String currentUser) {
        Like like = findOrThrow(tenantId, id);
        if (request.getReactionType() != null) like.setReactionType(request.getReactionType());
        like.setUpdatedBy(currentUser);
        return toResponse(likeRepository.save(like));
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        Like like = findOrThrow(tenantId, id);
        like.setDeleted(true);
        like.setUpdatedBy(currentUser);
        likeRepository.save(like);
    }

    private Like findOrThrow(String tenantId, UUID id) {
        return likeRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Like", "id", id));
    }

    private LikeDto.Response toResponse(Like like) {
        return LikeDto.Response.builder()
                .id(like.getId())
                .tenantId(like.getTenantId())
                .postId(like.getPostId())
                .commentId(like.getCommentId())
                .employeeId(like.getEmployeeId())
                .reactionType(like.getReactionType())
                .createdBy(like.getCreatedBy())
                .updatedBy(like.getUpdatedBy())
                .createdAt(like.getCreatedAt())
                .updatedAt(like.getUpdatedAt())
                .build();
    }
}
