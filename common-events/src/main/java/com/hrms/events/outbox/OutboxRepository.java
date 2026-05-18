package com.hrms.events.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    @Query("select o from OutboxEvent o where o.sentAt is null and o.attempts < :maxAttempts order by o.createdAt asc")
    List<OutboxEvent> findUnsent(int maxAttempts, Pageable pageable);
}
