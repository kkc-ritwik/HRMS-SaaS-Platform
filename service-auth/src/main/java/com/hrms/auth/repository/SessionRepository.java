package com.hrms.auth.repository;

import com.hrms.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByUserIdAndActiveTrue(UUID userId);

    long countByUserIdAndActiveTrue(UUID userId);

    Optional<Session> findByRefreshTokenHashAndActiveTrue(String refreshTokenHash);
}
