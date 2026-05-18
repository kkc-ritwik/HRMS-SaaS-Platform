package com.hrms.events.publisher;

import com.hrms.events.model.DomainEvent;

public interface EventPublisher {
    /** Publish via outbox (transactional, at-least-once). Use inside @Transactional service methods. */
    void publish(String topic, DomainEvent event);

    /** Direct send — bypasses outbox. Only safe when you don't need delivery guarantees. */
    void publishDirect(String topic, DomainEvent event);
}
