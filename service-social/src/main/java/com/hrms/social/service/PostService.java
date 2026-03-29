package com.hrms.social.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.social.dto.PostDto;
import com.hrms.social.entity.Post;
import com.hrms.social.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public PostDto.Response create(String tenantId, PostDto.CreateRequest request, String currentUser) {
        Post post = new Post();
        post.setTenantId(tenantId);
        post.setAuthorId(request.getAuthorId());
        post.setContent(request.getContent());
        post.setMediaUrls(request.getMediaUrls());
        post.setVisibility(request.getVisibility() != null ? request.getVisibility() : Post.Visibility.PUBLIC);
        post.setPostType(request.getPostType() != null ? request.getPostType() : Post.PostType.GENERAL);
        post.setPinned(request.isPinned());
        post.setLikesCount(0);
        post.setCommentsCount(0);
        post.setCreatedBy(currentUser);
        post.setUpdatedBy(currentUser);
        return toResponse(postRepository.save(post));
    }

    public PostDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    public Page<PostDto.Response> list(String tenantId, Pageable pageable) {
        return postRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    public List<PostDto.Response> listAll(String tenantId) {
        return postRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Page<PostDto.Response> listByAuthor(String tenantId, UUID authorId, Pageable pageable) {
        return postRepository.findByTenantIdAndAuthorIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, authorId, pageable)
                .map(this::toResponse);
    }

    public Page<PostDto.Response> listByVisibility(String tenantId, Post.Visibility visibility, Pageable pageable) {
        return postRepository.findByTenantIdAndVisibilityAndDeletedFalseOrderByCreatedAtDesc(tenantId, visibility, pageable)
                .map(this::toResponse);
    }

    public PostDto.Response update(String tenantId, UUID id, PostDto.UpdateRequest request, String currentUser) {
        Post post = findOrThrow(tenantId, id);
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getMediaUrls() != null) post.setMediaUrls(request.getMediaUrls());
        if (request.getVisibility() != null) post.setVisibility(request.getVisibility());
        if (request.getPostType() != null) post.setPostType(request.getPostType());
        if (request.getPinned() != null) post.setPinned(request.getPinned());
        post.setUpdatedBy(currentUser);
        return toResponse(postRepository.save(post));
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        Post post = findOrThrow(tenantId, id);
        post.setDeleted(true);
        post.setUpdatedBy(currentUser);
        postRepository.save(post);
    }

    private Post findOrThrow(String tenantId, UUID id) {
        return postRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
    }

    private PostDto.Response toResponse(Post post) {
        return PostDto.Response.builder()
                .id(post.getId())
                .tenantId(post.getTenantId())
                .authorId(post.getAuthorId())
                .content(post.getContent())
                .mediaUrls(post.getMediaUrls())
                .visibility(post.getVisibility())
                .postType(post.getPostType())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .pinned(post.isPinned())
                .createdBy(post.getCreatedBy())
                .updatedBy(post.getUpdatedBy())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
