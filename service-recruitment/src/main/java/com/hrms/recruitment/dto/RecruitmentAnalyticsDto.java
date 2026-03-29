package com.hrms.recruitment.dto;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecruitmentAnalyticsDto {

    /** Overall pipeline summary across all active requisitions. */
    @Getter @Setter @Builder
    public static class PipelineSummary {
        private long totalActive;         // ACTIVE requisitions
        private long totalApplications;
        private long totalHired;
        private long totalRejected;
        private long pendingScreening;
        private List<ApplicationDto.StageCount> stageCounts;
    }

    /** Time-to-hire stats per requisition (days from applied_at → stage=HIRED). */
    @Getter @Setter @Builder
    public static class TimeToHire {
        private UUID   requisitionId;
        private String requisitionTitle;
        private double avgDaysToHire;
        private double minDaysToHire;
        private double maxDaysToHire;
        private long   hiredCount;
    }

    /** Stage-by-stage conversion rates for a requisition. */
    @Getter @Setter @Builder
    public static class FunnelConversion {
        private UUID   requisitionId;
        private String requisitionTitle;
        private List<StageConversion> stages;
    }

    @Getter @Setter @Builder
    public static class StageConversion {
        private String stage;
        private long   count;
        private double conversionRate;   // % of previous stage that reached this one
    }

    /** Source effectiveness: how many leads, screens, hires per source. */
    @Getter @Setter @Builder
    public static class SourceEffectiveness {
        private String source;
        private long   totalApplications;
        private long   totalHired;
        private double hireRate;         // hiredCount / totalApplications * 100
    }

    /** Top-level analytics response bundling all metrics. */
    @Getter @Setter @Builder
    public static class DashboardResponse {
        private PipelineSummary             pipeline;
        private List<TimeToHire>            timeToHire;
        private List<FunnelConversion>      funnels;
        private List<SourceEffectiveness>   sourceEffectiveness;
        private Map<String, Long>           requisitionsByStatus;
    }
}
