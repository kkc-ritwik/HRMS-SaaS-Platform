package com.hrms.social.repository;

import com.hrms.social.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Post> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Post> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Page<Post> findByTenantIdAndAuthorIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, UUID authorId, Pageable pageable);

    Page<Post> findByTenantIdAndVisibilityAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Post.Visibility visibility, Pageable pageable);
}
