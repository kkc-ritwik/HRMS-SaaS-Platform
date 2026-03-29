package com.hrms.social.repository;

import com.hrms.social.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Optional<Comment> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Comment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Comment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Comment> findByTenantIdAndPostIdAndDeletedFalseOrderByCreatedAtAsc(String tenantId, UUID postId);
}
