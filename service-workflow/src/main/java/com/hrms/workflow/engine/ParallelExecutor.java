package com.hrms.workflow.engine;

import com.hrms.workflow.entity.WorkflowStep;
import com.hrms.workflow.entity.WorkflowStep.QuorumPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Determines whether a parallel group can advance — given the set of approvals/rejections
 * received so far, applies the group's quorum policy (ANY / ALL / MAJORITY).
 *
 * Returned states:
 *   PENDING — keep waiting (more decisions needed)
 *   APPROVED — group resolves favorably, workflow advances
 *   REJECTED — group resolves negatively, workflow rejected
 */
@Slf4j
@Service
public class ParallelExecutor {

    public GroupOutcome evaluate(List<WorkflowStep> stepsInGroup, Map<UUID, Decision> approverDecisions) {
        if (stepsInGroup.isEmpty()) return GroupOutcome.APPROVED;
        QuorumPolicy policy = stepsInGroup.get(0).getQuorumPolicy();
        if (policy == null) policy = QuorumPolicy.ALL;

        int total = stepsInGroup.size();
        long approved = approverDecisions.values().stream().filter(d -> d == Decision.APPROVED).count();
        long rejected = approverDecisions.values().stream().filter(d -> d == Decision.REJECTED).count();
        long pending = total - approved - rejected;

        return switch (policy) {
            case ANY -> {
                if (approved > 0) yield GroupOutcome.APPROVED;
                if (rejected == total) yield GroupOutcome.REJECTED;
                yield GroupOutcome.PENDING;
            }
            case ALL -> {
                if (rejected > 0) yield GroupOutcome.REJECTED;
                if (approved == total) yield GroupOutcome.APPROVED;
                yield GroupOutcome.PENDING;
            }
            case MAJORITY -> {
                int threshold = stepsInGroup.get(0).getQuorumThresholdPercent() == null
                        ? 51 : stepsInGroup.get(0).getQuorumThresholdPercent();
                int neededApprovals = (int) Math.ceil(total * threshold / 100.0);
                int neededToBlock = total - neededApprovals + 1;
                if (approved >= neededApprovals) yield GroupOutcome.APPROVED;
                if (rejected >= neededToBlock) yield GroupOutcome.REJECTED;
                yield GroupOutcome.PENDING;
            }
        };
    }

    /** Group consecutive steps that share parallel_group into batches in order. */
    public List<List<WorkflowStep>> partitionByGroup(List<WorkflowStep> stepsInOrder) {
        List<List<WorkflowStep>> batches = new ArrayList<>();
        List<WorkflowStep> current = new ArrayList<>();
        String currentGroup = null;
        for (WorkflowStep s : stepsInOrder) {
            String g = s.getParallelGroup();
            if (g == null) {
                if (!current.isEmpty()) { batches.add(current); current = new ArrayList<>(); }
                batches.add(List.of(s));
                currentGroup = null;
            } else if (g.equals(currentGroup)) {
                current.add(s);
            } else {
                if (!current.isEmpty()) batches.add(current);
                current = new ArrayList<>(List.of(s));
                currentGroup = g;
            }
        }
        if (!current.isEmpty()) batches.add(current);
        return batches;
    }

    public enum Decision { APPROVED, REJECTED }
    public enum GroupOutcome { PENDING, APPROVED, REJECTED }
}
