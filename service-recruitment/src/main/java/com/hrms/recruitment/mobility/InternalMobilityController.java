package com.hrms.recruitment.mobility;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Internal Job Board + Alumni Network + Boomerang rehire — single controller because
 * the three concepts share infrastructure (employee identity, requisition reuse).
 *
 *  Internal board:
 *    GET    /api/recruitment/internal/openings
 *    POST   /api/recruitment/internal/apply
 *    GET    /api/recruitment/internal/my-applications
 *    POST   /api/recruitment/internal/{id}/decide
 *
 *  Alumni network:
 *    GET    /api/recruitment/alumni
 *    POST   /api/recruitment/alumni            — register / update
 *    GET    /api/recruitment/alumni/boomerang  — rehire-eligible alumni
 *    POST   /api/recruitment/alumni/{id}/mark-rejoined
 */
@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
public class InternalMobilityController {

    public interface InternalAppRepo extends JpaRepository<InternalApplication, UUID> {
        @Query("SELECT i FROM InternalApplication i WHERE i.tenantId = :t AND i.employeeId = :e ORDER BY i.submittedAt DESC")
        List<InternalApplication> forEmployee(@Param("t") String tenant, @Param("e") UUID emp);

        @Query("SELECT i FROM InternalApplication i WHERE i.tenantId = :t AND i.requisitionId = :r")
        List<InternalApplication> forRequisition(@Param("t") String tenant, @Param("r") UUID req);
    }

    public interface AlumniRepo extends JpaRepository<AlumniRecord, UUID> {
        @Query("SELECT a FROM AlumniRecord a WHERE a.tenantId = :t AND a.boomerangEligible = true AND (a.doNotRehire IS NULL OR a.doNotRehire = false)")
        List<AlumniRecord> rehireEligible(@Param("t") String tenant);

        @Query("SELECT a FROM AlumniRecord a WHERE a.tenantId = :t ORDER BY a.exitDate DESC")
        List<AlumniRecord> all(@Param("t") String tenant);
    }

    private final InternalAppRepo apps;
    private final AlumniRepo alumni;

    // ── Internal board ──────────────────────────────────────────────────────

    @PostMapping("/internal/apply")
    @Transactional
    public InternalApplication apply(@RequestBody Map<String, Object> body) {
        InternalApplication a = new InternalApplication();
        a.setTenantId(TenantContext.get());
        a.setEmployeeId(UUID.fromString((String) body.get("employeeId")));
        a.setRequisitionId(UUID.fromString((String) body.get("requisitionId")));
        if (body.get("currentManagerId") != null)
            a.setCurrentManagerId(UUID.fromString((String) body.get("currentManagerId")));
        a.setCoverNote((String) body.get("coverNote"));
        a.setResumeUri((String) body.get("resumeUri"));
        a.setConfidentialMode(Boolean.TRUE.equals(body.get("confidentialMode")));
        a.setManagerNotified(!Boolean.TRUE.equals(body.get("confidentialMode")));
        a.setSubmittedAt(OffsetDateTime.now());
        return apps.save(a);
    }

    @GetMapping("/internal/my-applications")
    public List<InternalApplication> mine(@RequestParam UUID employeeId) {
        return apps.forEmployee(TenantContext.get(), employeeId);
    }

    @GetMapping("/internal/requisition/{requisitionId}/applicants")
    public List<InternalApplication> applicants(@org.springframework.web.bind.annotation.PathVariable UUID requisitionId) {
        return apps.forRequisition(TenantContext.get(), requisitionId);
    }

    @PostMapping("/internal/{id}/decide")
    @Transactional
    public InternalApplication decide(@org.springframework.web.bind.annotation.PathVariable UUID id,
                                      @RequestBody Map<String, Object> body) {
        InternalApplication a = apps.findById(id).orElseThrow();
        a.setStatus(InternalApplication.Status.valueOf((String) body.get("status")));
        a.setDecisionReason((String) body.get("reason"));
        a.setDecisionAt(OffsetDateTime.now());
        return apps.save(a);
    }

    // ── Alumni network ──────────────────────────────────────────────────────

    @GetMapping("/alumni")
    public List<AlumniRecord> listAlumni() {
        return alumni.all(TenantContext.get());
    }

    @PostMapping("/alumni")
    @Transactional
    public AlumniRecord registerAlumnus(@RequestBody AlumniRecord rec) {
        rec.setTenantId(TenantContext.get());
        // Default boomerang flag from rehire recommendation if not set
        if (rec.getBoomerangEligible() == null && rec.getRehireRecommended() != null) {
            rec.setBoomerangEligible(rec.getRehireRecommended() && !Boolean.TRUE.equals(rec.getDoNotRehire()));
        }
        return alumni.save(rec);
    }

    @GetMapping("/alumni/boomerang")
    public List<AlumniRecord> boomerang() {
        return alumni.rehireEligible(TenantContext.get());
    }

    @PostMapping("/alumni/{id}/mark-rejoined")
    @Transactional
    public AlumniRecord markRejoined(@org.springframework.web.bind.annotation.PathVariable UUID id,
                                     @RequestBody Map<String, Object> body) {
        AlumniRecord a = alumni.findById(id).orElseThrow();
        a.setRejoinedOn(java.time.LocalDate.parse((String) body.get("rejoinedOn")));
        a.setRejoinedEmployeeId(UUID.fromString((String) body.get("newEmployeeId")));
        return alumni.save(a);
    }
}
