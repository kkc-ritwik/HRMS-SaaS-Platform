package com.hrms.cases.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Internal Complaints Committee â€” required by POSH Act 2013 in India.
 * Each committee has a presiding officer + members; managed per tenant.
 */
@Entity
@Table(name = "icc_committees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Auditable("IccCommittee")
@EntityListeners(AuditEntityListener.class)
public class IccCommittee {
    @Id @GeneratedValue private UUID id;
    @Column(name = "tenant_id", length = 100, nullable = false) private String tenantId;
    @Column(length = 200, nullable = false) private String name;
    @Column(name = "presiding_officer_id", nullable = false) private UUID presidingOfficerId;
    @Column(length = 500) private String description;
    @Column(name = "is_active") private Boolean isActive;
}
