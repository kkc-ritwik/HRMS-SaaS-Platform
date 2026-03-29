package com.hrms.performance.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.performance.dto.PerformanceAnalyticsDto;
import com.hrms.performance.service.PerformanceAnalyticsService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/performance/analytics")
@RequiredArgsConstructor
@Tag(name = "Performance Analytics", description = "9-box grid and team performance summary analytics")
public class PerformanceAnalyticsController {

    private final PerformanceAnalyticsService analyticsService;

    @GetMapping("/cycles/{cycleId}/nine-box")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "9-box grid from FINALIZED manager reviews in a cycle " +
            "(X=performance band, Y=potential band, 3×3 grid with quadrant labels)")
    public ResponseEntity<ApiResponse<PerformanceAnalyticsDto.NineBoxGrid>> getNineBoxGrid(
            @PathVariable UUID cycleId) {
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getNineBoxGrid(tenantId(), cycleId)));
    }

    @GetMapping("/cycles/{cycleId}/team-summary/{managerId}")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Team performance summary for a manager in a cycle " +
            "(avg ratings, individual bands, 9-box cell labels)")
    public ResponseEntity<ApiResponse<PerformanceAnalyticsDto.TeamSummary>> getTeamSummary(
            @PathVariable UUID cycleId,
            @PathVariable UUID managerId) {
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getTeamSummary(tenantId(), cycleId, managerId)));
    }

    @GetMapping("/cycles/{cycleId}/my-team-summary")
    @PreAuthorize("hasAuthority('PERFORMANCE:READ')")
    @Operation(summary = "Team performance summary for the currently authenticated manager")
    public ResponseEntity<ApiResponse<PerformanceAnalyticsDto.TeamSummary>> myTeamSummary(
            @PathVariable UUID cycleId) {
        UUID managerId = UUID.fromString(principal().getEmployeeId());
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getTeamSummary(tenantId(), cycleId, managerId)));
    }

    private String tenantId() { return TenantContext.get(); }

    private UserPrincipal principal() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
