package com.hrms.workflow.engine;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.workflow.entity.WorkflowInstance;
import com.hrms.workflow.repository.WorkflowInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Runs every minute, finds PENDING/IN_PROGRESS workflow instances whose
 * current-step SLA has expired, flips them to ESCALATED, and publishes a
 * workflow.escalated event. Notification fan-out is handled by listeners
 * (service-notification subscribes and resolves the manager chain to alert).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaEscalator {

    private final WorkflowInstanceRepository instances;
    private final EventPublisher events;

    @Scheduled(fixedDelayString = "${hrms.workflow.sla.poll-ms:60000}")
    @Transactional
    public void scan() {
        List<WorkflowInstance> overdue = instances.findOverdue(Instant.now());
        for (WorkflowInstance wi : overdue) {
            wi.setStatus(WorkflowInstance.InstanceStatus.ESCALATED);
            wi.setEscalatedAt(Instant.now());
            instances.save(wi);
            events.publish(Topics.WORKFLOW, DomainEvent.of(
                    "workflow.escalated", "workflow",
                    wi.getTenantId(), wi.getId().toString(), "WorkflowInstance",
                    Map.of("entityType", wi.getEntityType(),
                           "entityId", wi.getEntityId(),
                           "currentStepOrder", wi.getCurrentStepOrder())));
            log.warn("Workflow instance {} escalated — SLA expired", wi.getId());
        }
    }
}
