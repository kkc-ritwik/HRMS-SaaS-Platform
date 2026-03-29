package com.hrms.common.event;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DomainEvent {
    @Builder.Default private String id = UUID.randomUUID().toString();
    private String type;
    private String tenantId;
    private String userId;
    private String entityType;
    private String entityId;
    private Object payload;
    @Builder.Default private Instant timestamp = Instant.now();
}
