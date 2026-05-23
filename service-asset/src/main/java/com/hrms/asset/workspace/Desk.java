package com.hrms.asset.workspace;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * A bookable desk / workspace. Belongs to a Floor + Office. dedicated=true → permanently
 * assigned to a single employee (not hot-desk); permanentEmployeeId is then non-null.
 */
@Entity
@Table(name = "workspace_desks", indexes = {
        @Index(name = "ix_desk_floor", columnList = "tenant_id,floor_id"),
        @Index(name = "ix_desk_code", columnList = "tenant_id,code", unique = true)
})
@Auditable("Desk")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Desk extends BaseEntity {

    @Column(name = "code", length = 30, nullable = false) private String code;
    @Column(name = "name", length = 100) private String name;

    @Column(name = "office_id") private UUID officeId;
    @Column(name = "floor_id") private UUID floorId;
    @Column(name = "zone", length = 100) private String zone;

    @Column(name = "x_coord") private Integer xCoord;
    @Column(name = "y_coord") private Integer yCoord;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private DeskType type = DeskType.HOT_DESK;

    @Column(name = "has_monitor") private Boolean hasMonitor;
    @Column(name = "has_phone") private Boolean hasPhone;
    @Column(name = "has_dock") private Boolean hasDock;
    @Column(name = "accessibility") private Boolean accessibilityFriendly;

    @Column(name = "dedicated") private Boolean dedicated;
    @Column(name = "permanent_employee_id") private UUID permanentEmployeeId;
    @Column(name = "active") private Boolean active = true;

    public enum DeskType { HOT_DESK, DEDICATED, COLLABORATION, FOCUS_POD, MEETING_ROOM, PHONE_BOOTH }
}
