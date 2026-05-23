package com.hrms.engagement.rewards;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * REST surface for budgets + catalog + redemptions.
 *   GET   /api/engagement/rewards/budgets           — list
 *   POST  /api/engagement/rewards/budgets           — create
 *   POST  /api/engagement/rewards/budgets/{id}/spend
 *   GET   /api/engagement/rewards/catalog
 *   POST  /api/engagement/rewards/catalog           — add item (admin)
 *   POST  /api/engagement/rewards/redeem
 *   POST  /api/engagement/rewards/redemptions/{id}/fulfill
 */
@RestController
@RequestMapping("/api/engagement/rewards")
@RequiredArgsConstructor
public class RewardsController {

    public interface BudgetRepo extends JpaRepository<RewardsBudget, UUID> {
        @Query("SELECT b FROM RewardsBudget b WHERE b.tenantId = :t AND b.active = true")
        List<RewardsBudget> active(@Param("t") String tenant);
    }

    public interface CatalogRepo extends JpaRepository<CatalogItem, UUID> {
        @Query("SELECT c FROM CatalogItem c WHERE c.tenantId = :t AND c.active = true " +
                "ORDER BY c.category, c.pointsCost")
        List<CatalogItem> active(@Param("t") String tenant);
    }

    public interface RedemptionRepo extends JpaRepository<Redemption, UUID> {
        @Query("SELECT r FROM Redemption r WHERE r.tenantId = :t AND r.employeeId = :e ORDER BY r.createdAt DESC")
        List<Redemption> forEmployee(@Param("t") String tenant, @Param("e") UUID emp);
    }

    private final BudgetRepo budgets;
    private final CatalogRepo catalog;
    private final RedemptionRepo redemptions;

    // ── Budgets ─────────────────────────────────────────────────────────────

    @GetMapping("/budgets")
    public List<RewardsBudget> listBudgets() { return budgets.active(TenantContext.get()); }

    @PostMapping("/budgets")
    public RewardsBudget createBudget(@RequestBody RewardsBudget b) {
        b.setTenantId(TenantContext.get());
        if (b.getConsumedAmount() == null) b.setConsumedAmount(BigDecimal.ZERO);
        return budgets.save(b);
    }

    @PostMapping("/budgets/{id}/spend")
    @Transactional
    public Map<String, Object> spend(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        RewardsBudget b = budgets.findById(id).orElseThrow();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        BigDecimal projected = b.getConsumedAmount().add(amount);

        boolean overdraft = projected.compareTo(b.getAllocatedAmount()) > 0;
        if (overdraft && !Boolean.TRUE.equals(b.getAllowOverdraft())) {
            throw new IllegalStateException("Budget exhausted — overdraft not allowed");
        }

        b.setConsumedAmount(projected);
        budgets.save(b);

        BigDecimal pct = projected
                .multiply(BigDecimal.valueOf(100))
                .divide(b.getAllocatedAmount(), 2, java.math.RoundingMode.HALF_UP);

        return Map.of(
                "budgetId", id,
                "allocated", b.getAllocatedAmount(),
                "consumed", projected,
                "percentUsed", pct,
                "overdraft", overdraft
        );
    }

    // ── Catalog ─────────────────────────────────────────────────────────────

    @GetMapping("/catalog")
    public List<CatalogItem> listCatalog() { return catalog.active(TenantContext.get()); }

    @PostMapping("/catalog")
    public CatalogItem addItem(@RequestBody CatalogItem item) {
        item.setTenantId(TenantContext.get());
        if (item.getActive() == null) item.setActive(true);
        return catalog.save(item);
    }

    // ── Redemption ──────────────────────────────────────────────────────────

    @PostMapping("/redeem")
    @Transactional
    public Redemption redeem(@RequestBody Map<String, Object> body) {
        UUID itemId = UUID.fromString((String) body.get("catalogItemId"));
        UUID employeeId = UUID.fromString((String) body.get("employeeId"));
        int qty = body.get("quantity") == null ? 1 : ((Number) body.get("quantity")).intValue();
        int availablePoints = ((Number) body.get("availablePoints")).intValue();

        CatalogItem item = catalog.findById(itemId).orElseThrow();
        int cost = item.getPointsCost() * qty;
        if (cost > availablePoints) {
            throw new IllegalStateException("Insufficient points: need " + cost + ", have " + availablePoints);
        }
        if (item.getStockQty() != null && item.getStockQty() < qty) {
            throw new IllegalStateException("Out of stock");
        }

        Redemption r = new Redemption();
        r.setTenantId(TenantContext.get());
        r.setEmployeeId(employeeId);
        r.setCatalogItemId(itemId);
        r.setQuantity(qty);
        r.setPointsSpent(cost);
        r.setShippingAddress((String) body.get("shippingAddress"));
        redemptions.save(r);

        if (item.getStockQty() != null) {
            item.setStockQty(item.getStockQty() - qty);
            catalog.save(item);
        }
        return r;
    }

    @PostMapping("/redemptions/{id}/fulfill")
    @Transactional
    public Redemption fulfill(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Redemption r = redemptions.findById(id).orElseThrow();
        r.setStatus(Redemption.Status.FULFILLED);
        r.setFulfilledAt(OffsetDateTime.now());
        r.setVoucherCodeEncrypted((String) body.get("voucherCode"));
        return redemptions.save(r);
    }

    @GetMapping("/redemptions/employee/{employeeId}")
    public List<Redemption> ofEmployee(@PathVariable UUID employeeId) {
        return redemptions.forEmployee(TenantContext.get(), employeeId);
    }
}
