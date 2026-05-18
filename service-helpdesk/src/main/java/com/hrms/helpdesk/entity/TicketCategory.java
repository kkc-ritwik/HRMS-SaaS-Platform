package com.hrms.helpdesk.entity;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "ticket_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Auditable("TicketCategory")
@EntityListeners(AuditEntityListener.class)
public class TicketCategory extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sla_hours", nullable = false)
    private int slaHours = 24;

    @Column(name = "auto_assign_to")
    private UUID autoAssignTo;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
