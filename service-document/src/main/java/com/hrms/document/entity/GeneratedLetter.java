package com.hrms.document.entity;

import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "generated_letters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedLetter extends BaseEntity {

    public enum LetterStatus {
        DRAFT, SENT, DELIVERED
    }

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "letter_type", length = 100)
    private String letterType;

    @Column(name = "subject", length = 300)
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "generated_by")
    private UUID generatedBy;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LetterStatus status = LetterStatus.DRAFT;
}
