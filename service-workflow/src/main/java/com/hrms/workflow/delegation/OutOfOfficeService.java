package com.hrms.workflow.delegation;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutOfOfficeService {

    public interface Repo extends JpaRepository<OutOfOffice, UUID> {
        @Query("SELECT o FROM OutOfOffice o WHERE o.userId = :userId AND o.active = true " +
                "AND o.deleted = false AND :now BETWEEN o.startsAt AND o.endsAt")
        Optional<OutOfOffice> findActiveForUser(@Param("userId") UUID userId, @Param("now") Instant now);

        List<OutOfOffice> findByTenantIdAndUserIdAndDeletedFalseOrderByStartsAtDesc(String tenantId, UUID userId);
    }

    private final Repo repo;

    @Transactional
    public OutOfOffice set(OutOfOffice o) {
        o.setTenantId(TenantContext.get()); o.setActive(true); return repo.save(o);
    }

    @Transactional
    public OutOfOffice cancel(UUID id) {
        OutOfOffice o = repo.findById(id).orElseThrow();
        o.setActive(false);
        return repo.save(o);
    }

    /** Returns the active OOO delegate for a user, or empty if no OOO is in effect. */
    public Optional<UUID> resolveDelegate(UUID userId) {
        return repo.findActiveForUser(userId, Instant.now()).map(OutOfOffice::getDelegateUserId);
    }

    public List<OutOfOffice> history(UUID userId) {
        return repo.findByTenantIdAndUserIdAndDeletedFalseOrderByStartsAtDesc(TenantContext.get(), userId);
    }
}
