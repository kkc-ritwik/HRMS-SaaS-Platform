package com.hrms.asset.workspace;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Floor / seating-map plate. The floorplanSvg field holds the editable SVG; zones is a
 * JSON array of named polygons that the front-end overlays on top of the SVG and on which
 * desks are pinned via Desk.xCoord/yCoord.
 */
@Entity
@Table(name = "workspace_floors",
        indexes = @Index(name = "ix_floor_office", columnList = "tenant_id,office_id"))
@Auditable("Floor")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Floor extends BaseEntity {

    @Column(name = "office_id", nullable = false) private UUID officeId;
    @Column(name = "name", length = 100, nullable = false) private String name;
    @Column(name = "level_number") private Integer levelNumber;
    @Column(name = "capacity") private Integer capacity;

    @Column(name = "width_px") private Integer widthPx;
    @Column(name = "height_px") private Integer heightPx;

    @Lob @Column(name = "floorplan_svg", columnDefinition = "TEXT") private String floorplanSvg;
    @Column(name = "floorplan_image_uri", length = 500) private String floorplanImageUri;

    /** [{ name:"East Wing", polygon:[[x,y],...], color:"#abc" }, ...] */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "zones", columnDefinition = "jsonb")
    private List<Map<String, Object>> zones;

    @Column(name = "active") private Boolean active = true;
}
