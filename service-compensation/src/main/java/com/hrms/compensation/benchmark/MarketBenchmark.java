package com.hrms.compensation.benchmark;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Imported market salary survey data — Mercer Total Remuneration Survey, AON Radford,
 * Willis Towers Watson, Glassdoor / Levels.fyi, or any internal survey. One row per
 * role/level/geo at a specific surveyDate.
 *
 * Integration with the live vendor APIs is a stub — the operations team uploads CSVs via
 * MarketBenchmarkController#bulkUpload or the field-team pulls via {@link BenchmarkProviderClient}.
 */
@Entity
@Table(name = "comp_market_benchmarks", indexes = {
        @Index(name = "ix_bench_role_geo", columnList = "tenant_id,role_code,country"),
        @Index(name = "ix_bench_date", columnList = "survey_date")
})
@Auditable("MarketBenchmark")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MarketBenchmark extends BaseEntity {

    @Column(name = "provider", length = 50, nullable = false) private String provider;     // MERCER / AON / WTW / GLASSDOOR / LEVELS_FYI / INTERNAL
    @Column(name = "role_code", length = 100, nullable = false) private String roleCode;
    @Column(name = "role_title", length = 200) private String roleTitle;
    @Column(name = "job_family", length = 100) private String jobFamily;
    @Column(name = "level", length = 30) private String level;
    @Column(name = "country", length = 2, nullable = false) private String country;
    @Column(name = "city", length = 100) private String city;
    @Column(name = "industry", length = 100) private String industry;
    @Column(name = "company_size_band", length = 30) private String companySizeBand;

    @Column(name = "currency", length = 3, nullable = false) private String currency;
    @Column(name = "p10_total_cash", precision = 14, scale = 2) private BigDecimal p10TotalCash;
    @Column(name = "p25_total_cash", precision = 14, scale = 2) private BigDecimal p25TotalCash;
    @Column(name = "p50_total_cash", precision = 14, scale = 2) private BigDecimal p50TotalCash;
    @Column(name = "p75_total_cash", precision = 14, scale = 2) private BigDecimal p75TotalCash;
    @Column(name = "p90_total_cash", precision = 14, scale = 2) private BigDecimal p90TotalCash;

    @Column(name = "p50_base", precision = 14, scale = 2) private BigDecimal p50Base;
    @Column(name = "p50_variable", precision = 14, scale = 2) private BigDecimal p50Variable;
    @Column(name = "p50_equity", precision = 14, scale = 2) private BigDecimal p50Equity;

    @Column(name = "sample_size") private Integer sampleSize;
    @Column(name = "survey_date", nullable = false) private LocalDate surveyDate;
    @Column(name = "source_document_uri", length = 500) private String sourceDocumentUri;

    @Column(name = "internal_pay_grade_id") private UUID internalPayGradeId;
}
