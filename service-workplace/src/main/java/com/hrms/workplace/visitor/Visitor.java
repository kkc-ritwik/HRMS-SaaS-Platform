package com.hrms.workplace.visitor;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/** Front-desk visitor log + badge issuance. */
@Entity
@Table(name = "visitors",
        indexes = @Index(name = "ix_visitor_date", columnList = "tenant_id,arrival_at"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Visitor extends BaseEntity {

    @Column(name = "full_name", length = 200, nullable = false) private String fullName;
    @Column(name = "company", length = 200) private String company;
    @Column(name = "email", length = 200) private String email;
    @Column(name = "phone", length = 30) private String phone;
    @Column(name = "purpose", length = 500) private String purpose;

    @Column(name = "host_employee_id") private UUID hostEmployeeId;
    @Column(name = "location_id") private UUID locationId;

    @Column(name = "arrival_at") private Instant arrivalAt;
    @Column(name = "expected_departure_at") private Instant expectedDepartureAt;
    @Column(name = "actual_departure_at") private Instant actualDepartureAt;

    @Column(name = "badge_number", length = 50) private String badgeNumber;
    @Column(name = "photo_uri", length = 1000) private String photoUri;
    @Column(name = "id_proof_type", length = 30) private String idProofType;
    @Column(name = "id_proof_number_masked", length = 250)
    @jakarta.persistence.Convert(converter = com.hrms.security.crypto.PiiEncryptedConverter.class)
    private String idProofNumberMasked;

    @Column(name = "nda_signed") private Boolean ndaSigned;
    @Column(name = "vehicle_number", length = 20) private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private Status status = Status.PRE_REGISTERED;

    public enum Status { PRE_REGISTERED, CHECKED_IN, CHECKED_OUT, NO_SHOW, BLACKLISTED }
}
