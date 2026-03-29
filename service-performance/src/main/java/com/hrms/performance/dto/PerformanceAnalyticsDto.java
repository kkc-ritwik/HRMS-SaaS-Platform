package com.hrms.performance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PerformanceAnalyticsDto {

    /** One cell in the 9-box grid. */
    @Getter @Setter @Builder
    public static class NineBoxCell {
        /** 1 = Low, 2 = Medium, 3 = High */
        private int                       performanceBand;
        private int                       potentialBand;
        private String                    label;   // e.g. "Star Performer"
        private List<EmployeePosition>    employees;
        private long                      count;
    }

    @Getter @Setter @Builder
    public static class EmployeePosition {
        private UUID       employeeId;
        private BigDecimal performanceRating;
        private int        potentialRating;
        private BigDecimal overallRating;
    }

    /** Full 9-box grid for a review cycle. */
    @Getter @Setter @Builder
    public static class NineBoxGrid {
        private UUID              cycleId;
        private String            cycleName;
        private List<NineBoxCell> grid;         // 9 cells, ordered perf 1→3, potential 1→3
        private long              totalReviewed;
    }

    /** Individual performance summary for team view. */
    @Getter @Setter @Builder
    public static class TeamMemberSummary {
        private UUID       employeeId;
        private BigDecimal overallRating;
        private BigDecimal goalScore;
        private BigDecimal competencyScore;
        private BigDecimal performanceRating;
        private int        potentialRating;
        private String     performanceBand;   // HIGH / MEDIUM / LOW
        private String     potentialBand;
        private String     nineBoxLabel;
    }

    @Getter @Setter @Builder
    public static class TeamSummary {
        private UUID                       cycleId;
        private UUID                       managerId;
        private List<TeamMemberSummary>    members;
        private BigDecimal                 avgOverallRating;
        private BigDecimal                 avgGoalScore;
        private BigDecimal                 avgCompetencyScore;
    }
}
