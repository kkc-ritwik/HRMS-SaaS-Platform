package com.hrms.recruitment.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.recruitment.dto.RecruitmentAnalyticsDto;
import com.hrms.recruitment.service.RecruitmentAnalyticsService;
import com.hrms.security.model.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recruitment/analytics")
@RequiredArgsConstructor
@Tag(name = "Recruitment Analytics", description = "Time-to-hire, pipeline funnel and source effectiveness metrics")
public class RecruitmentAnalyticsController {

    private final RecruitmentAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Full recruitment dashboard: pipeline, time-to-hire, source effectiveness")
    public ResponseEntity<ApiResponse<RecruitmentAnalyticsDto.DashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getDashboard(tenantId())));
    }

    @GetMapping("/pipeline")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Overall pipeline stage counts across all requisitions")
    public ResponseEntity<ApiResponse<RecruitmentAnalyticsDto.PipelineSummary>> pipeline() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getPipelineSummary(tenantId())));
    }

    @GetMapping("/time-to-hire")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Time-to-hire stats for all requisitions that have hired candidates")
    public ResponseEntity<ApiResponse<List<RecruitmentAnalyticsDto.TimeToHire>>> timeToHire() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getAllTimeToHire(tenantId())));
    }

    @GetMapping("/time-to-hire/{requisitionId}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Time-to-hire stats for a specific requisition")
    public ResponseEntity<ApiResponse<RecruitmentAnalyticsDto.TimeToHire>> timeToHireByJob(
            @PathVariable UUID requisitionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getTimeToHire(tenantId(), requisitionId)));
    }

    @GetMapping("/funnel/{requisitionId}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Stage-by-stage conversion funnel for a requisition")
    public ResponseEntity<ApiResponse<RecruitmentAnalyticsDto.FunnelConversion>> funnel(
            @PathVariable UUID requisitionId) {
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getFunnelConversion(tenantId(), requisitionId)));
    }

    @GetMapping("/source-effectiveness")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Hire rate by candidate source (agency, referral, job portal, etc.)")
    public ResponseEntity<ApiResponse<List<RecruitmentAnalyticsDto.SourceEffectiveness>>> sourceEffectiveness() {
        return ResponseEntity.ok(ApiResponse.ok(
                analyticsService.getSourceEffectiveness(tenantId())));
    }

    private String tenantId() { return TenantContext.get(); }
}
