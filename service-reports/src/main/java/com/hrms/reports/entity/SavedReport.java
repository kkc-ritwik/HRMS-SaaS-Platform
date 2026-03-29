package com.hrms.reports.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "saved_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavedReport extends BaseEntity {

    public enum ReportFormat {
        PDF, EXCEL, CSV
    }

    public enum ReportStatus {
        PENDING, COMPLETED, FAILED
    }

    @Column(name = "definition_id", nullable = false)
    private UUID definitionId;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters", columnDefinition = "jsonb")
    private List<String> filters;

    @Column(name = "generated_by", nullable = false)
    private UUID generatedBy;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_format", nullable = false, length = 20)
    private ReportFormat fileFormat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
