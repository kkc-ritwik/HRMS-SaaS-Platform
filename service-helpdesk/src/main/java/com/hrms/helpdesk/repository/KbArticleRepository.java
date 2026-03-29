package com.hrms.helpdesk.repository;

import com.hrms.helpdesk.entity.KbArticle;
import com.hrms.helpdesk.entity.KbArticle.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KbArticleRepository extends JpaRepository<KbArticle, UUID> {

    Optional<KbArticle> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<KbArticle> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<KbArticle> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Page<KbArticle> findByTenantIdAndStatusAndDeletedFalse(
            String tenantId, ArticleStatus status, Pageable pageable);
}
