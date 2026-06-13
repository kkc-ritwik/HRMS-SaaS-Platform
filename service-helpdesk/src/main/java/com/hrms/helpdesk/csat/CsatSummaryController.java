package com.hrms.helpdesk.csat;

import com.hrms.common.dto.ApiResponse;
import com.hrms.helpdesk.entity.Ticket;
import com.hrms.helpdesk.repository.TicketRepository;
import com.hrms.security.model.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Helpdesk CSAT (Customer Satisfaction) summary — aggregates the per-ticket
 * satisfaction ratings (1–5) into counts, average and a CSAT% score.
 */
@RestController
@RequestMapping("/api/v1/helpdesk/tickets/csat")
@RequiredArgsConstructor
@Tag(name = "Helpdesk CSAT", description = "Customer satisfaction analytics")
public class CsatSummaryController {

    private final TicketRepository ticketRepository;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('HELPDESK:READ')")
    @Operation(summary = "CSAT summary — response count, average score, CSAT% and distribution")
    public ResponseEntity<ApiResponse<Map<String, Object>>> summary() {
        List<Ticket> tickets = ticketRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(TenantContext.get());

        Map<Integer, Long> dist = new TreeMap<>();
        for (int i = 1; i <= 5; i++) dist.put(i, 0L);
        long count = 0;
        long sum = 0;
        long satisfied = 0;   // ratings 4–5
        for (Ticket t : tickets) {
            Integer r = t.getSatisfactionRating();
            if (r == null || r < 1 || r > 5) continue;
            dist.merge(r, 1L, Long::sum);
            count++;
            sum += r;
            if (r >= 4) satisfied++;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("responses", count);
        out.put("average", count == 0 ? 0.0
                : BigDecimal.valueOf((double) sum / count).setScale(2, RoundingMode.HALF_UP).doubleValue());
        out.put("csatPercent", count == 0 ? 0.0
                : BigDecimal.valueOf(satisfied * 100.0 / count).setScale(1, RoundingMode.HALF_UP).doubleValue());
        out.put("distribution", dist);
        return ResponseEntity.ok(ApiResponse.ok(out));
    }
}
