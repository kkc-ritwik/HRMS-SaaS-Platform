package com.hrms.asset.visitor;

import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Visitor pass / front-desk check-in record. Captures who came to the office, when,
 * who they met, identity proof, and check-out time. PII (phone, email, ID) is encrypted.
 *
 * Workflow:
 *   1. Host pre-invites (PRE_REGISTERED) — sends invite email with QR.
 *   2. Visitor arrives, scans QR or front-desk searches → CHECKED_IN, photo captured.
 *   3. Host notified.
 *   4. On exit → CHECKED_OUT.
 *   5. Pass auto-expires same day (END_OF_DAY housekeeping).
 */
@Entity
@Table(name = "workspace_visitors", indexes = {
        @Index(name = "ix_visitor_date", columnList = "tenant_id,visit_date"),
        @Index(name = "ix_visitor_host", columnList = "tenant_id,host_employee_id")
})
@Auditable(value = "Visitor", redactFields = "phone,email,idNumber,vehicleNumber")
@EntityListeners(AuditEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Visitor extends BaseEntity {

    @Column(name = "full_name", length = 200, nullable = false) private String fullName;
    @Column(name = "company", length = 200) private String company;
    @Column(name = "purpose", length = 500) private String purpose;

    @Column(name = "phone", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String phone;

    @Column(name = "email", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String email;

    @Column(name = "id_type", length = 30) private String idType;          // AADHAAR / PASSPORT / DL / OFFICE_ID
    @Column(name = "id_number", length = 200)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String idNumber;

    @Column(name = "vehicle_number", length = 100)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String vehicleNumber;

    @Column(name = "host_employee_id") private UUID hostEmployeeId;
    @Column(name = "location_id") private UUID locationId;
    @Column(name = "floor_id") private UUID floorId;
    @Column(name = "badge_number", length = 50) private String badgeNumber;
    @Column(name = "photo_uri", length = 500) private String photoUri;
    @Column(name = "qr_token", length = 100) private String qrToken;

    @Column(name = "visit_date") private java.time.LocalDate visitDate;
    @Column(name = "expected_arrival") private OffsetDateTime expectedArrival;
    @Column(name = "checked_in_at") private OffsetDateTime checkedInAt;
    @Column(name = "checked_out_at") private OffsetDateTime checkedOutAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.PRE_REGISTERED;

    @Column(name = "nda_signed") private Boolean ndaSigned;
    @Column(name = "nda_document_uri", length = 500) private String ndaDocumentUri;
    @Column(name = "temperature_check") private String temperatureCheck;
    @Column(name = "health_declaration") private Boolean healthDeclaration;

    public enum Status { PRE_REGISTERED, CHECKED_IN, CHECKED_OUT, NO_SHOW, EXPIRED, DENIED }
}
