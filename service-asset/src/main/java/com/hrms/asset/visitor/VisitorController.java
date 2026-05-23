package com.hrms.asset.visitor;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/asset/visitors")
@RequiredArgsConstructor
public class VisitorController {

    public interface Repo extends JpaRepository<Visitor, UUID> {
        @Query("SELECT v FROM Visitor v WHERE v.tenantId = :t AND v.visitDate = :d ORDER BY v.expectedArrival")
        List<Visitor> onDate(@Param("t") String tenant, @Param("d") LocalDate date);

        @Query("SELECT v FROM Visitor v WHERE v.tenantId = :t AND v.hostEmployeeId = :h AND v.visitDate >= :d")
        List<Visitor> forHost(@Param("t") String tenant, @Param("h") UUID host, @Param("d") LocalDate from);

        Optional<Visitor> findByQrToken(String token);
    }

    private final Repo repo;

    @PostMapping("/pre-register")
    @Transactional
    public Visitor preRegister(@RequestBody Visitor v) {
        v.setTenantId(TenantContext.get());
        v.setQrToken(UUID.randomUUID().toString());
        v.setStatus(Visitor.Status.PRE_REGISTERED);
        return repo.save(v);
    }

    @PostMapping("/check-in")
    @Transactional
    public Visitor checkIn(@RequestBody Map<String, Object> body) {
        String token = (String) body.get("qrToken");
        Visitor v = token != null ? repo.findByQrToken(token).orElse(null)
                : (body.get("visitorId") != null ? repo.findById(UUID.fromString((String) body.get("visitorId"))).orElseThrow() : null);
        if (v == null) {
            v = new Visitor();
            v.setTenantId(TenantContext.get());
            v.setFullName((String) body.get("fullName"));
            v.setCompany((String) body.get("company"));
            v.setPhone((String) body.get("phone"));
            v.setPurpose((String) body.get("purpose"));
            if (body.get("hostEmployeeId") != null) v.setHostEmployeeId(UUID.fromString((String) body.get("hostEmployeeId")));
            v.setVisitDate(LocalDate.now());
            v.setQrToken(UUID.randomUUID().toString());
        }
        v.setStatus(Visitor.Status.CHECKED_IN);
        v.setCheckedInAt(OffsetDateTime.now());
        v.setBadgeNumber((String) body.get("badgeNumber"));
        v.setPhotoUri((String) body.get("photoUri"));
        v.setIdType((String) body.get("idType"));
        v.setIdNumber((String) body.get("idNumber"));
        v.setNdaSigned(Boolean.TRUE.equals(body.get("ndaSigned")));
        v.setHealthDeclaration(Boolean.TRUE.equals(body.get("healthDeclaration")));
        return repo.save(v);
    }

    @PostMapping("/{id}/check-out")
    @Transactional
    public Visitor checkOut(@PathVariable UUID id) {
        Visitor v = repo.findById(id).orElseThrow();
        v.setStatus(Visitor.Status.CHECKED_OUT);
        v.setCheckedOutAt(OffsetDateTime.now());
        return repo.save(v);
    }

    @GetMapping("/today")
    public List<Visitor> today() { return repo.onDate(TenantContext.get(), LocalDate.now()); }

    @GetMapping("/host/{employeeId}")
    public List<Visitor> forHost(@PathVariable UUID employeeId,
                                 @RequestParam(required = false) LocalDate from) {
        return repo.forHost(TenantContext.get(), employeeId, from == null ? LocalDate.now().minusDays(30) : from);
    }
}
