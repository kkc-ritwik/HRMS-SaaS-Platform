package com.hrms.recruitment.repository;

import com.hrms.recruitment.entity.InterviewPanelist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterviewPanelistRepository extends JpaRepository<InterviewPanelist, UUID> {

    List<InterviewPanelist> findByInterviewIdAndDeletedFalse(UUID interviewId);

    Optional<InterviewPanelist> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Optional<InterviewPanelist> findByInterviewIdAndInterviewerIdAndDeletedFalse(
            UUID interviewId, UUID interviewerId);

    boolean existsByInterviewIdAndInterviewerIdAndDeletedFalse(UUID interviewId, UUID interviewerId);
}
