package com.hrms.corehr.costcenter;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/corehr/cost-centers")
@RequiredArgsConstructor
public class CostCenterController {

    public interface Repo extends JpaRepository<CostCenter, UUID> {
        @Query("SELECT c FROM CostCenter c WHERE c.tenantId = :t AND c.active = true ORDER BY c.code")
        List<CostCenter> active(@Param("t") String tenant);
    }

    public interface AllocRepo extends JpaRepository<CostCenterAllocation, UUID> {
        @Query("SELECT a FROM CostCenterAllocation a WHERE a.tenantId = :t AND a.employeeId = :e " +
                "AND (a.effectiveTo IS NULL OR a.effectiveTo >= :on) AND a.effectiveFrom <= :on")
        List<CostCenterAllocation> activeOnDate(@Param("t") String tenant,
                                                @Param("e") UUID emp, @Param("on") LocalDate on);
    }

    private final Repo repo;
    private final AllocRepo allocRepo;

    @GetMapping
    public List<CostCenter> list() { return repo.active(TenantContext.get()); }

    @PostMapping
    @Transactional
    public CostCenter create(@RequestBody CostCenter cc) {
        cc.setTenantId(TenantContext.get());
        if (cc.getActive() == null) cc.setActive(true);
        return repo.save(cc);
    }

    @PostMapping("/allocations")
    @Transactional
    public List<CostCenterAllocation> allocate(@RequestBody List<CostCenterAllocation> alloc) {
        BigDecimal total = BigDecimal.ZERO;
        UUID empId = null;
        for (CostCenterAllocation a : alloc) {
            a.setTenantId(TenantContext.get());
            total = total.add(a.getPercentage());
            empId = a.getEmployeeId();
        }
        if (total.compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException("Allocations must sum to 100% (got " + total + ")");
        }
        return allocRepo.saveAll(alloc);
    }

    @GetMapping("/employee/{employeeId}/active")
    public List<CostCenterAllocation> currentAllocation(@PathVariable UUID employeeId,
                                                        @RequestParam(required = false) LocalDate on) {
        return allocRepo.activeOnDate(TenantContext.get(), employeeId,
                on == null ? LocalDate.now() : on);
    }
}
