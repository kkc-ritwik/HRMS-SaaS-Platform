package com.hrms.helpdesk.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.helpdesk.dto.KbArticleDto;
import com.hrms.helpdesk.entity.KbArticle;
import com.hrms.helpdesk.entity.KbArticle.ArticleStatus;
import com.hrms.helpdesk.repository.KbArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KbArticleService {

    private final KbArticleRepository kbArticleRepository;

    @Transactional
    public KbArticleDto.Response create(String tenantId, String currentUser,
                                        KbArticleDto.CreateRequest request) {
        KbArticle article = new KbArticle();
        article.setTenantId(tenantId);
        article.setCreatedBy(currentUser);
        article.setUpdatedBy(currentUser);
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategoryId(request.getCategoryId());
        article.setTags(request.getTags());
        article.setStatus(ArticleStatus.DRAFT);
        article.setAuthorId(request.getAuthorId());
        return toResponse(kbArticleRepository.save(article));
    }

    @Transactional(readOnly = true)
    public KbArticleDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<KbArticleDto.Response> list(String tenantId, Pageable pageable) {
        return kbArticleRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<KbArticleDto.Response> listAll(String tenantId) {
        return kbArticleRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<KbArticleDto.Response> listPublished(String tenantId, Pageable pageable) {
        return kbArticleRepository
                .findByTenantIdAndStatusAndDeletedFalse(tenantId, ArticleStatus.PUBLISHED, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public KbArticleDto.Response update(String tenantId, UUID id, String currentUser,
                                        KbArticleDto.UpdateRequest request) {
        KbArticle article = findOrThrow(tenantId, id);
        if (request.getTitle() != null) article.setTitle(request.getTitle());
        if (request.getContent() != null) article.setContent(request.getContent());
        if (request.getCategoryId() != null) article.setCategoryId(request.getCategoryId());
        if (request.getTags() != null) article.setTags(request.getTags());
        if (request.getStatus() != null) article.setStatus(request.getStatus());
        if (request.getPublishedAt() != null) article.setPublishedAt(request.getPublishedAt());
        article.setUpdatedBy(currentUser);
        return toResponse(kbArticleRepository.save(article));
    }

    @Transactional
    public KbArticleDto.Response publish(String tenantId, UUID id, String currentUser) {
        KbArticle article = findOrThrow(tenantId, id);
        article.setStatus(ArticleStatus.PUBLISHED);
        article.setPublishedAt(Instant.now());
        article.setUpdatedBy(currentUser);
        return toResponse(kbArticleRepository.save(article));
    }

    @Transactional
    public KbArticleDto.Response incrementViews(String tenantId, UUID id) {
        KbArticle article = findOrThrow(tenantId, id);
        article.setViews(article.getViews() + 1);
        return toResponse(kbArticleRepository.save(article));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        KbArticle article = findOrThrow(tenantId, id);
        article.setDeleted(true);
        article.setUpdatedBy(currentUser);
        kbArticleRepository.save(article);
    }

    private KbArticle findOrThrow(String tenantId, UUID id) {
        return kbArticleRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("KbArticle", "id", id));
    }

    private KbArticleDto.Response toResponse(KbArticle a) {
        return KbArticleDto.Response.builder()
                .id(a.getId())
                .tenantId(a.getTenantId())
                .title(a.getTitle())
                .content(a.getContent())
                .categoryId(a.getCategoryId())
                .tags(a.getTags())
                .status(a.getStatus())
                .views(a.getViews())
                .helpfulVotes(a.getHelpfulVotes())
                .authorId(a.getAuthorId())
                .publishedAt(a.getPublishedAt())
                .createdBy(a.getCreatedBy())
                .updatedBy(a.getUpdatedBy())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
