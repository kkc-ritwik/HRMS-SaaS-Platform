package com.hrms.recruitment.repository;

import com.hrms.recruitment.entity.OfferLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferLetterRepository extends JpaRepository<OfferLetter, UUID> {

    Optional<OfferLetter> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<OfferLetter> findByApplicationIdAndTenantIdAndDeletedFalseOrderByCreatedAtDesc(
            UUID applicationId, String tenantId);

    List<OfferLetter> findByTenantIdAndStatusAndDeletedFalse(
            String tenantId, OfferLetter.OfferStatus status);

    List<OfferLetter> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
}
