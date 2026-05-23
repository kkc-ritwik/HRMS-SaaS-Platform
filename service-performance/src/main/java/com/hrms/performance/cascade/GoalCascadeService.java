package com.hrms.performance.cascade;

import com.hrms.performance.entity.Goal;
import com.hrms.performance.repository.GoalRepository;
import com.hrms.security.model.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Cascading engine — Org → Department → Team → Individual.
 *
 * Given a parent Goal (e.g. CEO objective "Grow ARR 30%"), this service spawns
 * child Goals across N assignees. The parent's progress becomes a weighted roll-up
 * of children, so updating a leaf KR percolates up to the org-level objective.
 *
 * Aggregation is on-demand via {@link #recomputeRollup(UUID)}; the GoalUpdate service
 * should call it whenever a child progress changes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalCascadeService {

    private final GoalRepository goals;
    @PersistenceContext private EntityManager em;

    /**
     * Spawn child goals from a parent. Each child inherits title (optionally suffixed),
     * cycle, due date, type (typically KEY_RESULT or INDIVIDUAL) and a per-assignee weightage.
     * If weightages don't sum to 100 they are normalised proportionally.
     */
    @Transactional
    public List<UUID> cascade(CascadeRequest req) {
        Goal parent = goals.findById(req.parentGoalId)
                .orElseThrow(() -> new IllegalArgumentException("Parent goal not found"));

        if (req.assignees == null || req.assignees.isEmpty()) {
            throw new IllegalArgumentException("No assignees supplied");
        }

        BigDecimal totalWeight = req.assignees.stream()
                .map(a -> a.weightage == null ? BigDecimal.ONE : a.weightage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<UUID> created = new ArrayList<>();
        String tenant = TenantContext.get();

        for (Assignee a : req.assignees) {
            Goal child = new Goal();
            child.setTenantId(tenant);
            child.setEmployeeId(a.employeeId);
            child.setManagerId(a.managerId);
            child.setCycleId(parent.getCycleId());
            child.setParentGoalId(parent.getId());

            child.setTitle(a.titleOverride != null && !a.titleOverride.isBlank()
                    ? a.titleOverride
                    : parent.getTitle());
            child.setDescription(a.descriptionOverride != null
                    ? a.descriptionOverride
                    : parent.getDescription());

            child.setGoalType(req.childType != null ? req.childType : Goal.GoalType.INDIVIDUAL);
            child.setStatus(Goal.GoalStatus.ACTIVE);
            child.setPriority(parent.getPriority());
            child.setStartDate(parent.getStartDate());
            child.setDueDate(parent.getDueDate());

            BigDecimal w = a.weightage == null ? BigDecimal.ONE : a.weightage;
            if (totalWeight.signum() != 0) {
                w = w.multiply(BigDecimal.valueOf(100))
                        .divide(totalWeight, 2, RoundingMode.HALF_UP);
            }
            child.setWeightage(w);
            child.setProgress(BigDecimal.ZERO);
            child.setTargetValue(a.targetValue);
            child.setUnit(a.unit != null ? a.unit : parent.getUnit());

            goals.save(child);
            created.add(child.getId());
        }

        log.info("Cascaded goal {} into {} children", parent.getId(), created.size());
        return created;
    }

    /**
     * Recompute parent's progress = sum(child.progress * child.weightage) / sum(child.weightage).
     * Walks upwards recursively so multi-level cascades stay consistent.
     */
    @Transactional
    public BigDecimal recomputeRollup(UUID goalId) {
        Goal goal = goals.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        Object[] row = (Object[]) em.createNativeQuery(
                "SELECT COALESCE(SUM(progress * weightage), 0), COALESCE(SUM(weightage), 0) " +
                "FROM goals WHERE parent_goal_id = :pid AND is_deleted = false")
                .setParameter("pid", goalId)
                .getSingleResult();

        BigDecimal weightedSum = toBd(row[0]);
        BigDecimal weightSum = toBd(row[1]);
        if (weightSum.signum() == 0) return goal.getProgress(); // leaf — leave alone

        BigDecimal rolled = weightedSum.divide(weightSum, 2, RoundingMode.HALF_UP);
        goal.setProgress(rolled);
        if (rolled.compareTo(BigDecimal.valueOf(100)) >= 0) {
            goal.setStatus(Goal.GoalStatus.COMPLETED);
        }
        goals.save(goal);

        if (goal.getParentGoalId() != null) {
            recomputeRollup(goal.getParentGoalId());
        }
        return rolled;
    }

    /** Full subtree below a root — useful for org-chart style visualisations. */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> subtree(UUID rootGoalId) {
        List<Object[]> rows = em.createNativeQuery(
                "WITH RECURSIVE tree AS (" +
                "  SELECT id, parent_goal_id, employee_id, title, weightage, progress, status, 0 AS depth " +
                "  FROM goals WHERE id = :root AND is_deleted = false " +
                "  UNION ALL " +
                "  SELECT g.id, g.parent_goal_id, g.employee_id, g.title, g.weightage, g.progress, g.status, t.depth+1 " +
                "  FROM goals g JOIN tree t ON g.parent_goal_id = t.id WHERE g.is_deleted = false" +
                ") SELECT * FROM tree ORDER BY depth, id")
                .setParameter("root", rootGoalId)
                .getResultList();

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id", r[0].toString());
            n.put("parentId", r[1] == null ? null : r[1].toString());
            n.put("employeeId", r[2] == null ? null : r[2].toString());
            n.put("title", r[3]);
            n.put("weightage", r[4]);
            n.put("progress", r[5]);
            n.put("status", r[6]);
            n.put("depth", r[7]);
            out.add(n);
        }
        return out;
    }

    private static BigDecimal toBd(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal b) return b;
        return new BigDecimal(o.toString());
    }

    public static class CascadeRequest {
        public UUID parentGoalId;
        public Goal.GoalType childType;
        public List<Assignee> assignees;
    }

    public static class Assignee {
        public UUID employeeId;
        public UUID managerId;
        public String titleOverride;
        public String descriptionOverride;
        public BigDecimal weightage;
        public BigDecimal targetValue;
        public String unit;
    }
}
