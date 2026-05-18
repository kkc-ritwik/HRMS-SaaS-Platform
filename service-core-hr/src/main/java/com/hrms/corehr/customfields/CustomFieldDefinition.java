package com.hrms.corehr.customfields;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

/**
 * Tenant-defined custom field on a built-in entity (employee, leave, expense, etc.).
 * The actual values are stored in the entity's JSONB custom_fields column; this table
 * is the registry that tells the UI how to render + validate.
 */
@Entity
@Table(name = "custom_field_definitions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","entity_name","field_key"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CustomFieldDefinition extends BaseEntity {

    /** Logical entity name â€” "Employee", "LeaveApplication", "Expense", "Asset", â€¦ */
    @Column(name = "entity_name", length = 100, nullable = false)
    private String entityName;

    /** Storage key inside the entity's custom_fields JSONB. e.g. "favourite_colour" */
    @Column(name = "field_key", length = 100, nullable = false)
    private String fieldKey;

    @Column(name = "label", length = 200, nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 30, nullable = false)
    private DataType dataType;

    @Column(name = "is_required", nullable = false) private boolean required = false;
    @Column(name = "is_searchable", nullable = false) private boolean searchable = false;
    @Column(name = "is_sensitive", nullable = false) private boolean sensitive = false;

    /** For SELECT / MULTISELECT â€” the allowed option list. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    private List<String> options;

    /** Optional validation rules â€” minLength/maxLength/min/max/regex. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation", columnDefinition = "jsonb")
    private Map<String, Object> validation;

    @Column(name = "default_value", length = 1000) private String defaultValue;
    @Column(name = "help_text", length = 500) private String helpText;
    @Column(name = "display_order") private Integer displayOrder;
    @Column(name = "section", length = 100) private String section;
    @Column(name = "is_active", nullable = false) private boolean active = true;

    public enum DataType {
        TEXT, TEXTAREA, NUMBER, DECIMAL, BOOLEAN, DATE, DATETIME,
        SELECT, MULTISELECT, EMAIL, URL, PHONE, FILE, EMPLOYEE_REF, JSON
    }
}
