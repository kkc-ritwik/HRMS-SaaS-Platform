package com.hrms.onboarding.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.onboarding.service.OnboardingTaskService;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Onboarding "workflows" view for the Onboarding board. A workflow is the per-new-hire grouping
 * of onboarding tasks; until a dedicated workflow aggregate exists, this surfaces the task-based
 * view so the UI integrates cleanly.
 */
@RestController
@RequestMapping("/api/v1/onboarding/workflows")
@RequiredArgsConstructor
public class OnboardingWorkflowController {

    private final OnboardingTaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Object>>> list() {
        // No standalone workflow aggregate yet — return an empty board so the page loads.
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("id", id, "tasks", List.of())));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<ApiResponse<List<Object>>> tasks(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }
}
