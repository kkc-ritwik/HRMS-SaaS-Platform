package com.hrms.performance.cascade;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/performance/goals/cascade")
@RequiredArgsConstructor
public class GoalCascadeController {

    private final GoalCascadeService cascadeService;

    @PostMapping
    public Map<String, Object> cascade(@RequestBody GoalCascadeService.CascadeRequest req) {
        List<UUID> created = cascadeService.cascade(req);
        return Map.of("parentGoalId", req.parentGoalId, "createdChildren", created);
    }

    @PostMapping("/{goalId}/rollup")
    public Map<String, Object> rollup(@PathVariable UUID goalId) {
        BigDecimal progress = cascadeService.recomputeRollup(goalId);
        return Map.of("goalId", goalId, "rolledProgress", progress);
    }

    @GetMapping("/{rootGoalId}/subtree")
    public List<Map<String, Object>> subtree(@PathVariable UUID rootGoalId) {
        return cascadeService.subtree(rootGoalId);
    }
}
