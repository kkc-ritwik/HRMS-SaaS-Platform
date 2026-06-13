package com.hrms.engagement.award;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AwardRepository extends JpaRepository<Award, UUID> {

    Optional<Award> findByIdAndTenantId(UUID id, String tenantId);

    List<Award> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    List<Award> findByTenantIdAndStatusOrderByCreatedAtDesc(String tenantId, Award.Status status);

    List<Award> findByTenantIdAndNomineeIdOrderByCreatedAtDesc(String tenantId, UUID nomineeId);
}
