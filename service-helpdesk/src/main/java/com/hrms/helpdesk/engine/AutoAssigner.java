package com.hrms.helpdesk.engine;

import com.hrms.helpdesk.entity.Ticket;
import com.hrms.helpdesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Picks the support agent with the fewest active tickets for round-robin assignment.
 * Pool of candidates is configured per category via env (HRMS_HELPDESK_POOL_<CATEGORY>=uuid1,uuid2).
 * Default pool falls back to a global pool.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoAssigner {

    private final TicketRepository tickets;

    @Value("${hrms.helpdesk.default-pool:}") private String defaultPoolCsv;

    public UUID pickAssignee(String tenantId, UUID categoryId) {
        List<UUID> pool = poolFor(categoryId);
        if (pool.isEmpty()) return null;
        Map<UUID, Long> loads = new HashMap<>();
        for (Object[] row : tickets.openTicketCountsByAssignee(tenantId)) {
            loads.put((UUID) row[0], (Long) row[1]);
        }
        return pool.stream()
                .min(Comparator.comparingLong(u -> loads.getOrDefault(u, 0L)))
                .orElse(null);
    }

    public void assignIfUnassigned(Ticket t) {
        if (t.getAssigneeId() != null) return;
        UUID a = pickAssignee(t.getTenantId(), t.getCategoryId());
        if (a != null) {
            t.setAssigneeId(a);
            log.info("Auto-assigned ticket {} to {}", t.getId(), a);
        }
    }

    private List<UUID> poolFor(UUID categoryId) {
        // Category-specific pools could be looked up from a DB table; for now use the default pool.
        if (defaultPoolCsv == null || defaultPoolCsv.isBlank()) return List.of();
        List<UUID> ids = new ArrayList<>();
        for (String s : defaultPoolCsv.split(",")) {
            try { ids.add(UUID.fromString(s.trim())); } catch (IllegalArgumentException ignored) {}
        }
        return ids;
    }
}
