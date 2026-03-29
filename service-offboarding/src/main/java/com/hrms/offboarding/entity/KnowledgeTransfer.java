package com.hrms.offboarding.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "knowledge_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeTransfer extends BaseEntity {

    public enum TransferStatus {
        PENDING, IN_PROGRESS, COMPLETED
    }

    @Column(name = "separation_id", nullable = false)
    private UUID separationId;

    @Column(name = "from_employee_id", nullable = false)
    private UUID fromEmployeeId;

    @Column(name = "to_employee_id", nullable = false)
    private UUID toEmployeeId;

    @Column(name = "topic", nullable = false, length = 300)
    private String topic;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;
}
