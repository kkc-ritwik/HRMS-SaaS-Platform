package com.hrms.performance.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.performance.dto.GoalDto;
import com.hrms.performance.entity.Goal;
import com.hrms.performance.entity.GoalUpdate;
import com.hrms.performance.repository.GoalRepository;
import com.hrms.performance.repository.GoalUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository       goalRepository;
    private final GoalUpdateRepository updateRepository;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional
    public GoalDto.Response create(String tenantId, GoalDto.CreateRequest req, String currentUser) {
        Goal g = new Goal();
        g.setTenantId(tenantId);
        g.setCreatedBy(currentUser);
        applyCreate(g, req);
        return toResponse(goalRepository.save(g));
    }

    @Transactional
    public GoalDto.Response update(String tenantId, UUID id,
                                    GoalDto.UpdateRequest req, String currentUser) {
        Goal g = getEntity(tenantId, id);
        if (req.getTitle() != null)         g.setTitle(req.getTitle());
        if (req.getDescription() != null)   g.setDescription(req.getDescription());
        if (req.getWeightage() != null)     g.setWeightage(req.getWeightage());
        if (req.getTargetValue() != null)   g.setTargetValue(req.getTargetValue());
        if (req.getUnit() != null)          g.setUnit(req.getUnit());
        if (req.getPriority() != null)      g.setPriority(req.getPriority());
        if (req.getStartDate() != null)     g.setStartDate(req.getStartDate());
        if (req.getDueDate() != null)       g.setDueDate(req.getDueDate());
        if (req.getStatus() != null)        g.setStatus(req.getStatus());
        g.setUpdatedBy(currentUser);
        return toResponse(goalRepository.save(g));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Goal g = getEntity(tenantId, id);
        g.setDeleted(true);
        g.setUpdatedBy(currentUser);
        goalRepository.save(g);
    }

    // ── Progress update ───────────────────────────────────────────────────────

    @Transactional
    public GoalDto.UpdateResponse updateProgress(String tenantId, UUID goalId,
                                                  GoalDto.ProgressUpdateRequest req,
                                                  String currentUser) {
        Goal g = getEntity(tenantId, goalId);

        GoalUpdate upd = new GoalUpdate();
        upd.setTenantId(tenantId);
        upd.setGoalId(goalId);
        upd.setProgressValue(req.getProgressValue());
        upd.setCurrentValue(req.getCurrentValue());
        upd.setComment(req.getComment());
        upd.setCreatedBy(currentUser);
        updateRepository.save(upd);

        // Update the goal's own progress / current value
        g.setProgress(req.getProgressValue());
        if (req.getCurrentValue() != null) g.setCurrentValue(req.getCurrentValue());
        if (req.getProgressValue().compareTo(BigDecimal.valueOf(100)) == 0) {
            g.setStatus(Goal.GoalStatus.COMPLETED);
        }
        g.setUpdatedBy(currentUser);
        goalRepository.save(g);

        // If this is a KEY_RESULT, recompute the parent OBJECTIVE's progress
        if (g.getParentGoalId() != null) {
            recomputeObjectiveProgress(tenantId, g.getParentGoalId(), currentUser);
        }

        return GoalDto.UpdateResponse.builder()
                .id(upd.getId()).goalId(goalId)
                .progressValue(req.getProgressValue())
                .currentValue(req.getCurrentValue())
                .comment(req.getComment())
                .createdAt(upd.getCreatedAt())
                .build();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GoalDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<GoalDto.Response> listForEmployee(String tenantId, UUID employeeId,
                                                   Pageable pageable) {
        return goalRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
                        tenantId, employeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<GoalDto.Response> listByCycle(String tenantId, UUID cycleId) {
        return goalRepository.findByTenantIdAndCycleIdAndDeletedFalse(tenantId, cycleId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<GoalDto.UpdateResponse> getUpdates(String tenantId, UUID goalId) {
        getEntity(tenantId, goalId); // access check
        return updateRepository.findByGoalIdAndTenantIdAndDeletedFalseOrderByCreatedAtDesc(goalId, tenantId)
                .stream().map(u -> GoalDto.UpdateResponse.builder()
                        .id(u.getId()).goalId(goalId)
                        .progressValue(u.getProgressValue())
                        .currentValue(u.getCurrentValue())
                        .comment(u.getComment())
                        .createdAt(u.getCreatedAt())
                        .build())
                .toList();
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void recomputeObjectiveProgress(String tenantId, UUID objectiveId, String currentUser) {
        Goal objective = getEntity(tenantId, objectiveId);
        List<Goal> krs = goalRepository.findByParentGoalIdAndTenantIdAndDeletedFalse(
                objectiveId, tenantId);
        if (krs.isEmpty()) return;

        BigDecimal totalWeightage = krs.stream()
                .map(Goal::getWeightage).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedProgress = krs.stream()
                .map(kr -> kr.getProgress().multiply(kr.getWeightage()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newProgress = totalWeightage.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : weightedProgress.divide(totalWeightage, 2, RoundingMode.HALF_UP);

        objective.setProgress(newProgress);
        objective.setUpdatedBy(currentUser);
        goalRepository.save(objective);
    }

    Goal getEntity(String tenantId, UUID id) {
        return goalRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
    }

    private void applyCreate(Goal g, GoalDto.CreateRequest req) {
        g.setEmployeeId(req.getEmployeeId());
        g.setManagerId(req.getManagerId());
        g.setCycleId(req.getCycleId());
        g.setParentGoalId(req.getParentGoalId());
        g.setGoalType(req.getGoalType());
        g.setTitle(req.getTitle());
        g.setDescription(req.getDescription());
        g.setWeightage(req.getWeightage() != null ? req.getWeightage() : BigDecimal.valueOf(100));
        g.setTargetValue(req.getTargetValue());
        g.setUnit(req.getUnit());
        g.setPriority(req.getPriority());
        g.setStartDate(req.getStartDate());
        g.setDueDate(req.getDueDate());
        g.setStatus(Goal.GoalStatus.DRAFT);
        g.setProgress(BigDecimal.ZERO);
    }

    GoalDto.Response toResponse(Goal g) {
        List<GoalDto.Response> krs = null;
        if (g.getGoalType() == Goal.GoalType.OBJECTIVE) {
            krs = goalRepository.findByParentGoalIdAndTenantIdAndDeletedFalse(
                            g.getId(), g.getTenantId())
                    .stream().map(this::toResponse).toList();
        }
        return GoalDto.Response.builder()
                .id(g.getId()).employeeId(g.getEmployeeId()).managerId(g.getManagerId())
                .cycleId(g.getCycleId()).parentGoalId(g.getParentGoalId())
                .goalType(g.getGoalType()).title(g.getTitle()).description(g.getDescription())
                .weightage(g.getWeightage()).targetValue(g.getTargetValue())
                .currentValue(g.getCurrentValue()).unit(g.getUnit())
                .progress(g.getProgress()).status(g.getStatus()).priority(g.getPriority())
                .startDate(g.getStartDate()).dueDate(g.getDueDate())
                .keyResults(krs)
                .createdAt(g.getCreatedAt()).updatedAt(g.getUpdatedAt())
                .build();
    }
}
