package com.hrms.clients.workflow;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "service-workflow", path = "/api/v1/workflow-instances")
public interface WorkflowClient {

    @PostMapping("/start")
    WorkflowInstanceDto start(@RequestBody StartRequest req);

    record StartRequest(String tenantId, String workflowCode, String referenceType,
                        String referenceId, Map<String, Object> context) {}

    record WorkflowInstanceDto(UUID id, String status, Integer currentStep) {}
}
