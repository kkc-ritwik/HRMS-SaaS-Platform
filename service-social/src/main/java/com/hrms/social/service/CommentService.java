package com.hrms.social.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.social.dto.CommentDto;
import com.hrms.social.entity.Comment;
import com.hrms.social.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentDto.Response create(String tenantId, CommentDto.CreateRequest request, String currentUser) {
        Comment comment = new Comment();
        comment.setTenantId(tenantId);
        comment.setPostId(request.getPostId());
        comment.setAuthorId(request.getAuthorId());
        comment.setContent(request.getContent());
        comment.setParentCommentId(request.getParentCommentId());
        comment.setLikesCount(0);
        comment.setCreatedBy(currentUser);
        comment.setUpdatedBy(currentUser);
        return toResponse(commentRepository.save(comment));
    }

    public CommentDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    public Page<CommentDto.Response> list(String tenantId, Pageable pageable) {
        return commentRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    public List<CommentDto.Response> listByPost(String tenantId, UUID postId) {
        return commentRepository.findByTenantIdAndPostIdAndDeletedFalseOrderByCreatedAtAsc(tenantId, postId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CommentDto.Response update(String tenantId, UUID id, CommentDto.UpdateRequest request, String currentUser) {
        Comment comment = findOrThrow(tenantId, id);
        if (request.getContent() != null) comment.setContent(request.getContent());
        comment.setUpdatedBy(currentUser);
        return toResponse(commentRepository.save(comment));
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        Comment comment = findOrThrow(tenantId, id);
        comment.setDeleted(true);
        comment.setUpdatedBy(currentUser);
        commentRepository.save(comment);
    }

    private Comment findOrThrow(String tenantId, UUID id) {
        return commentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }

    private CommentDto.Response toResponse(Comment comment) {
        return CommentDto.Response.builder()
                .id(comment.getId())
                .tenantId(comment.getTenantId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .likesCount(comment.getLikesCount())
                .createdBy(comment.getCreatedBy())
                .updatedBy(comment.getUpdatedBy())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
