package com.hrms.leave.service;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.leave.entity.ShiftSwapRequest;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftSwapService {

    public interface Repo extends JpaRepository<ShiftSwapRequest, UUID> {
        Page<ShiftSwapRequest> findByTenantIdAndStatus(String tenantId, ShiftSwapRequest.Status s, Pageable p);
    }

    private final Repo repo;
    private final ObjectProvider<EventPublisher> eventsProvider;

    @Transactional
    public ShiftSwapRequest request(ShiftSwapRequest req) {
        req.setTenantId(TenantContext.get());
        req.setStatus(ShiftSwapRequest.Status.PENDING_PARTNER);
        ShiftSwapRequest saved = repo.save(req);
        publish("leave.shift_swap.requested", saved);
        return saved;
    }

    @Transactional
    public ShiftSwapRequest partnerAccept(UUID id) {
        ShiftSwapRequest r = repo.findById(id).orElseThrow();
        if (r.getStatus() != ShiftSwapRequest.Status.PENDING_PARTNER) {
            throw new IllegalStateException("Not awaiting partner response");
        }
        r.setStatus(ShiftSwapRequest.Status.PENDING_MANAGER);
        r.setPartnerRespondedAt(Instant.now());
        ShiftSwapRequest saved = repo.save(r);
        publish("leave.shift_swap.partner_accepted", saved);
        return saved;
    }

    @Transactional
    public ShiftSwapRequest partnerDecline(UUID id, String reason) {
        ShiftSwapRequest r = repo.findById(id).orElseThrow();
        r.setStatus(ShiftSwapRequest.Status.REJECTED);
        r.setPartnerRespondedAt(Instant.now());
        r.setRejectionReason(reason);
        ShiftSwapRequest saved = repo.save(r);
        publish("leave.shift_swap.partner_declined", saved);
        return saved;
    }

    @Transactional
    public ShiftSwapRequest managerApprove(UUID id, UUID managerId) {
        ShiftSwapRequest r = repo.findById(id).orElseThrow();
        if (r.getStatus() != ShiftSwapRequest.Status.PENDING_MANAGER) {
            throw new IllegalStateException("Not awaiting manager approval");
        }
        r.setStatus(ShiftSwapRequest.Status.APPROVED);
        r.setManagerId(managerId);
        r.setManagerApprovedAt(Instant.now());
        ShiftSwapRequest saved = repo.save(r);
        publish("leave.shift_swap.approved", saved);
        return saved;
    }

    @Transactional
    public ShiftSwapRequest managerReject(UUID id, UUID managerId, String reason) {
        ShiftSwapRequest r = repo.findById(id).orElseThrow();
        r.setStatus(ShiftSwapRequest.Status.REJECTED);
        r.setManagerId(managerId);
        r.setRejectionReason(reason);
        ShiftSwapRequest saved = repo.save(r);
        publish("leave.shift_swap.rejected", saved);
        return saved;
    }

    private void publish(String type, ShiftSwapRequest r) {
        EventPublisher ev = eventsProvider.getIfAvailable();
        if (ev != null) {
            ev.publish(Topics.LEAVE, DomainEvent.of(type, "leave-attendance",
                    r.getTenantId(), r.getId().toString(), "ShiftSwapRequest",
                    Map.of("requesterId", r.getRequesterEmployeeId(),
                           "partnerId", r.getSwapWithEmployeeId(),
                           "status", r.getStatus())));
        }
    }
}
