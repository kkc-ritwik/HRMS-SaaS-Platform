package com.hrms.lms.scorm;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import com.hrms.tenant.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Uploaded SCORM 1.2 / 2004 package. The .zip is unpacked into MinIO and the launch
 * URL points to the entry point declared in imsmanifest.xml. Player at the front-end
 * loads the launch page and proxies the SCORM API to ScormTrackerController.
 */
@Entity
@Table(name = "lms_scorm_packages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","course_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScormPackage extends BaseEntity {

    @Column(name = "course_id", nullable = false) private UUID courseId;
    @Column(name = "scorm_version", length = 20, nullable = false) private String scormVersion;  // "1.2" / "2004"
    @Column(name = "package_storage_uri", length = 1000, nullable = false) private String packageStorageUri;
    @Column(name = "extracted_base_uri", length = 1000) private String extractedBaseUri;
    @Column(name = "launch_url", length = 1000, nullable = false) private String launchUrl;
    @Column(name = "manifest_identifier", length = 200) private String manifestIdentifier;
    @Column(name = "default_organization", length = 200) private String defaultOrganization;
    @Column(name = "is_active") private Boolean active;
}
