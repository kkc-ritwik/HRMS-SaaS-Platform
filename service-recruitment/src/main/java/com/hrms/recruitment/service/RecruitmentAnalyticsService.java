package com.hrms.recruitment.service;

import com.hrms.recruitment.dto.ApplicationDto;
import com.hrms.recruitment.dto.RecruitmentAnalyticsDto;
import com.hrms.recruitment.entity.Application;
import com.hrms.recruitment.entity.Application.ApplicationStage;
import com.hrms.recruitment.entity.JobRequisition;
import com.hrms.recruitment.repository.ApplicationRepository;
import com.hrms.recruitment.repository.CandidateRepository;
import com.hrms.recruitment.repository.JobRequisitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentAnalyticsService {

    private final ApplicationRepository    applicationRepository;
    private final JobRequisitionRepository requisitionRepository;
    private final CandidateRepository      candidateRepository;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RecruitmentAnalyticsDto.DashboardResponse getDashboard(String tenantId) {
        return RecruitmentAnalyticsDto.DashboardResponse.builder()
                .pipeline(getPipelineSummary(tenantId))
                .timeToHire(getAllTimeToHire(tenantId))
                .sourceEffectiveness(getSourceEffectiveness(tenantId))
                .requisitionsByStatus(getRequisitionsByStatus(tenantId))
                .build();
    }

    // ── Pipeline summary ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RecruitmentAnalyticsDto.PipelineSummary getPipelineSummary(String tenantId) {
        List<Object[]> rows = applicationRepository.stageBreakdownAll(tenantId);

        Map<ApplicationStage, Long> stageCounts = rows.stream()
                .collect(Collectors.toMap(
                        r -> (ApplicationStage) r[0],
                        r -> (Long) r[1]
                ));

        long total    = stageCounts.values().stream().mapToLong(Long::longValue).sum();
        long hired    = stageCounts.getOrDefault(ApplicationStage.HIRED, 0L);
        long rejected = stageCounts.getOrDefault(ApplicationStage.REJECTED, 0L);
        long pending  = stageCounts.getOrDefault(ApplicationStage.SCREENING, 0L)
                      + stageCounts.getOrDefault(ApplicationStage.APPLIED, 0L);

        long activeReqs = requisitionRepository
                .countByStatus(tenantId).stream()
                .filter(r -> r[0].toString().equals("ACTIVE"))
                .mapToLong(r -> (Long) r[1]).sum();

        List<ApplicationDto.StageCount> counts = stageCounts.entrySet().stream()
                .map(e -> ApplicationDto.StageCount.builder()
                        .stage(e.getKey()).count(e.getValue()).build())
                .sorted(Comparator.comparing(sc -> sc.getStage().ordinal()))
                .toList();

        return RecruitmentAnalyticsDto.PipelineSummary.builder()
                .totalActive(activeReqs)
                .totalApplications(total)
                .totalHired(hired)
                .totalRejected(rejected)
                .pendingScreening(pending)
                .stageCounts(counts)
                .build();
    }

    // ── Time-to-hire ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RecruitmentAnalyticsDto.TimeToHire> getAllTimeToHire(String tenantId) {
        return requisitionRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId,
                        org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(r -> computeTimeToHire(tenantId, r))
                .filter(t -> t.getHiredCount() > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public RecruitmentAnalyticsDto.TimeToHire getTimeToHire(String tenantId, UUID requisitionId) {
        JobRequisition r = requisitionRepository
                .findByIdAndTenantIdAndDeletedFalse(requisitionId, tenantId)
                .orElseThrow(() -> new com.hrms.common.exception.ResourceNotFoundException(
                        "JobRequisition", "id", requisitionId));
        return computeTimeToHire(tenantId, r);
    }

    // ── Funnel conversion ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RecruitmentAnalyticsDto.FunnelConversion getFunnelConversion(
            String tenantId, UUID requisitionId) {
        JobRequisition r = requisitionRepository
                .findByIdAndTenantIdAndDeletedFalse(requisitionId, tenantId)
                .orElseThrow(() -> new com.hrms.common.exception.ResourceNotFoundException(
                        "JobRequisition", "id", requisitionId));

        List<Object[]> rows = applicationRepository.stageBreakdown(tenantId, requisitionId);
        Map<ApplicationStage, Long> stageMap = rows.stream().collect(
                Collectors.toMap(row -> (ApplicationStage) row[0], row -> (Long) row[1]));

        ApplicationStage[] ordered = {
                ApplicationStage.APPLIED, ApplicationStage.SCREENING,
                ApplicationStage.PHONE_SCREEN, ApplicationStage.TECHNICAL,
                ApplicationStage.HR, ApplicationStage.OFFER, ApplicationStage.HIRED
        };

        List<RecruitmentAnalyticsDto.StageConversion> stages = new ArrayList<>();
        long prev = -1;
        for (ApplicationStage stage : ordered) {
            long count = stageMap.getOrDefault(stage, 0L);
            double rate = (prev > 0) ? (count * 100.0 / prev) : 100.0;
            stages.add(RecruitmentAnalyticsDto.StageConversion.builder()
                    .stage(stage.name())
                    .count(count)
                    .conversionRate(Math.round(rate * 10.0) / 10.0)
                    .build());
            prev = count == 0 ? prev : count; // don't reset denominator on empty stage
        }

        return RecruitmentAnalyticsDto.FunnelConversion.builder()
                .requisitionId(r.getId())
                .requisitionTitle(r.getTitle())
                .stages(stages)
                .build();
    }

    // ── Source effectiveness ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RecruitmentAnalyticsDto.SourceEffectiveness> getSourceEffectiveness(String tenantId) {
        // Load all apps + candidates to compute per-source metrics in Java
        List<Application> apps = applicationRepository.findByTenantIdAndDeletedFalse(tenantId);

        // Build candidateId → source map
        Map<UUID, String> candidateSource = new HashMap<>();
        for (Application app : apps) {
            if (!candidateSource.containsKey(app.getCandidateId())) {
                candidateRepository.findByIdAndTenantIdAndDeletedFalse(
                        app.getCandidateId(), tenantId)
                        .ifPresent(c -> candidateSource.put(c.getId(), c.getSource()));
            }
        }

        // Group apps by source
        Map<String, List<Application>> bySource = apps.stream()
                .collect(Collectors.groupingBy(
                        app -> candidateSource.getOrDefault(app.getCandidateId(), "UNKNOWN")));

        return bySource.entrySet().stream().map(entry -> {
            String source = entry.getKey();
            List<Application> sourceApps = entry.getValue();
            long total = sourceApps.size();
            long hired = sourceApps.stream()
                    .filter(a -> a.getStage() == ApplicationStage.HIRED).count();
            double hireRate = total > 0 ? Math.round(hired * 1000.0 / total) / 10.0 : 0.0;
            return RecruitmentAnalyticsDto.SourceEffectiveness.builder()
                    .source(source)
                    .totalApplications(total)
                    .totalHired(hired)
                    .hireRate(hireRate)
                    .build();
        }).sorted(Comparator.comparingDouble(RecruitmentAnalyticsDto.SourceEffectiveness::getHireRate)
                .reversed())
          .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private RecruitmentAnalyticsDto.TimeToHire computeTimeToHire(String tenantId,
                                                                   JobRequisition r) {
        List<Application> hired = applicationRepository
                .findByTenantIdAndRequisitionIdAndStageAndDeletedFalse(
                        tenantId, r.getId(), ApplicationStage.HIRED);

        if (hired.isEmpty()) {
            return RecruitmentAnalyticsDto.TimeToHire.builder()
                    .requisitionId(r.getId())
                    .requisitionTitle(r.getTitle())
                    .hiredCount(0)
                    .avgDaysToHire(0).minDaysToHire(0).maxDaysToHire(0)
                    .build();
        }

        DoubleSummaryStatistics stats = hired.stream()
                .mapToDouble(app -> {
                    long seconds = app.getStageChangedAt().getEpochSecond()
                                 - app.getAppliedAt().getEpochSecond();
                    return seconds / 86400.0;
                })
                .summaryStatistics();

        return RecruitmentAnalyticsDto.TimeToHire.builder()
                .requisitionId(r.getId())
                .requisitionTitle(r.getTitle())
                .hiredCount(stats.getCount())
                .avgDaysToHire(Math.round(stats.getAverage() * 10.0) / 10.0)
                .minDaysToHire(Math.round(stats.getMin() * 10.0) / 10.0)
                .maxDaysToHire(Math.round(stats.getMax() * 10.0) / 10.0)
                .build();
    }

    private Map<String, Long> getRequisitionsByStatus(String tenantId) {
        return requisitionRepository.countByStatus(tenantId).stream()
                .collect(Collectors.toMap(
                        r -> r[0].toString(),
                        r -> (Long) r[1]));
    }
}
