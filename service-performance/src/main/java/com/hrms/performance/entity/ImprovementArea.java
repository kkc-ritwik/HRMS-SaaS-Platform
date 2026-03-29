package com.hrms.performance.entity;

import lombok.*;

import java.time.LocalDate;

/** POJO serialised into the pip_plans.improvement_areas JSONB column. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImprovementArea {
    private String    area;
    private String    goal;
    private String    metric;
    private LocalDate deadline;
    /** NOT_STARTED, IN_PROGRESS, COMPLETED */
    private String    status;
}
