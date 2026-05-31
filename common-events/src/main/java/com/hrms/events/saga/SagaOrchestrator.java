package com.hrms.events.saga;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Orchestrator for saga state machines. Each step emits a command on the wire, then
 * waits for a reply (success or failure). On success → advance. On failure → flip into
 * compensating mode and walk back through completed steps.
 *
 * Step descriptor structure (in {@code SagaInstance.steps}):
 *   { "name":"provisionEmail", "service":"core-hr", "command":"email.provision",
 *     "compensation":"email.deprovision",
 *     "status":"PENDING|RUNNING|DONE|FAILED|COMPENSATED" }
 *
 * Replies are matched on {@code header.sagaId} which we put on every command.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "hrms.saga.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SagaOrchestrator {

    public interface Repo extends JpaRepository<SagaInstance, UUID> {}

    private final Repo repo;
    private final ObjectProvider<EventPublisher> publisherProvider;

    @Transactional
    public SagaInstance start(String tenant, String name, String subjectId,
                              List<Map<String, Object>> steps, Map<String, Object> context) {
        SagaInstance s = new SagaInstance();
        s.setTenantId(tenant);
        s.setName(name);
        s.setSubjectId(subjectId);
        s.setSteps(new ArrayList<>(steps));
        s.setContext(context);
        s.setTrace(new ArrayList<>());
        s.setCurrentStep(0);
        s.setStatus(SagaInstance.Status.RUNNING);
        repo.save(s);
        dispatchCurrent(s);
        return s;
    }

    @Transactional
    public void onStepReply(UUID sagaId, String stepName, boolean success, String errorMessage) {
        SagaInstance s = repo.findById(sagaId).orElseThrow();
        record(s, stepName, success ? "DONE" : "FAILED", errorMessage);
        if (!success) {
            log.warn("Saga {} step {} failed: {}", sagaId, stepName, errorMessage);
            s.setCompensating(true);
            s.setStatus(SagaInstance.Status.COMPENSATING);
            s.setErrorMessage(errorMessage);
            compensateBackwards(s);
            return;
        }
        s.setCurrentStep(s.getCurrentStep() + 1);
        if (s.getCurrentStep() >= s.getSteps().size()) {
            s.setStatus(SagaInstance.Status.COMPLETED);
            s.setFinishedAt(OffsetDateTime.now());
            repo.save(s);
            return;
        }
        repo.save(s);
        dispatchCurrent(s);
    }

    private void dispatchCurrent(SagaInstance s) {
        Map<String, Object> step = s.getSteps().get(s.getCurrentStep());
        String topic = (String) step.getOrDefault("topic", Topics.WORKFLOW);
        String command = (String) step.get("command");
        EventPublisher p = publisherProvider.getIfAvailable();
        if (p == null) {
            log.error("No EventPublisher available; saga {} stalled at step {}", s.getId(), step.get("name"));
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("sagaId", s.getId().toString());
        payload.put("subjectId", s.getSubjectId());
        payload.put("context", s.getContext());
        if (step.get("payload") instanceof Map<?, ?> m) payload.putAll(castMap(m));

        p.publishDirect(topic, DomainEvent.of(command, "saga-orchestrator",
                s.getTenantId(), s.getId().toString(), "Saga", payload));
        record(s, (String) step.get("name"), "RUNNING", null);
    }

    private void compensateBackwards(SagaInstance s) {
        EventPublisher p = publisherProvider.getIfAvailable();
        if (p == null) return;
        for (int i = s.getCurrentStep(); i >= 0; i--) {
            Map<String, Object> step = s.getSteps().get(i);
            if (!"DONE".equals(step.get("status"))) continue;
            String comp = (String) step.get("compensation");
            if (comp == null) continue;
            p.publishDirect((String) step.getOrDefault("topic", Topics.WORKFLOW),
                    DomainEvent.of(comp, "saga-orchestrator",
                            s.getTenantId(), s.getId().toString(), "Saga",
                            Map.of("sagaId", s.getId().toString(), "step", step.get("name"))));
            record(s, (String) step.get("name"), "COMPENSATED", null);
        }
        s.setStatus(SagaInstance.Status.COMPENSATED);
        s.setFinishedAt(OffsetDateTime.now());
        repo.save(s);
    }

    private void record(SagaInstance s, String stepName, String outcome, String error) {
        if (s.getTrace() == null) s.setTrace(new ArrayList<>());
        s.getTrace().add(Map.of(
                "step", stepName == null ? "" : stepName,
                "outcome", outcome,
                "at", OffsetDateTime.now().toString(),
                "error", error == null ? "" : error));
        // Mirror status on the step descriptor too
        for (Map<String, Object> step : s.getSteps()) {
            if (stepName != null && stepName.equals(step.get("name"))) {
                step.put("status", outcome);
            }
        }
        repo.save(s);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> m) {
        Map<String, Object> out = new HashMap<>();
        for (var e : m.entrySet()) out.put(String.valueOf(e.getKey()), e.getValue());
        return out;
    }
}
