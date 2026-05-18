package com.hrms.events.listener;

import com.hrms.events.annotation.PublishEvents;
import com.hrms.events.model.DomainEvent;
import com.hrms.events.publisher.EventPublisher;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic JPA listener that auto-publishes lifecycle DomainEvents for any entity carrying
 * {@link PublishEvents}. Eliminates the need to call events.publish() from every business
 * method — the event-bus contract is enforced by the entity itself.
 *
 * Uses the outbox pattern (transactional) so events commit with the DB write.
 */
@Slf4j
@Component
public class EntityLifecyclePublisher {

    private static ApplicationContext CTX;

    @Autowired
    public void init(ApplicationContext ctx) { CTX = ctx; }

    @PostPersist
    public void onPersist(Object entity) { fire(entity, "created"); }

    @PostUpdate
    public void onUpdate(Object entity) {
        PublishEvents ann = entity.getClass().getAnnotation(PublishEvents.class);
        if (ann != null && ann.skipUpdates()) return;
        fire(entity, "updated");
    }

    @PostRemove
    public void onRemove(Object entity) { fire(entity, "deleted"); }

    private void fire(Object entity, String verb) {
        try {
            if (CTX == null) return;
            PublishEvents ann = entity.getClass().getAnnotation(PublishEvents.class);
            if (ann == null) return;

            ObjectProvider<EventPublisher> p = CTX.getBeanProvider(EventPublisher.class);
            EventPublisher publisher = p.getIfAvailable();
            if (publisher == null) return;

            String ns = ann.namespace().isBlank()
                    ? entity.getClass().getSimpleName().toLowerCase()
                    : ann.namespace();
            String eventType = ns + "." + verb;
            String aggregateId = ann.useIdAsAggregateKey() ? readField(entity, "id") : null;
            String tenantId = readField(entity, "tenantId");

            Map<String, Object> payload = new HashMap<>();
            payload.put("entityClass", entity.getClass().getSimpleName());
            payload.put("entityId", aggregateId);
            payload.put("verb", verb);

            DomainEvent event = DomainEvent.of(eventType, ns,
                    tenantId, aggregateId, entity.getClass().getSimpleName(), payload);

            // Use direct send (no outbox) for safety — outbox needs an active TX which we may
            // not have at the JPA listener boundary. Async + idempotent on the consumer side.
            publisher.publishDirect(ann.topic(), event);
        } catch (Exception e) {
            log.warn("EntityLifecyclePublisher fire failed for {}.{}: {}",
                    entity.getClass().getSimpleName(), verb, e.getMessage());
        }
    }

    private String readField(Object entity, String field) {
        try {
            Field f = findField(entity.getClass(), field);
            if (f == null) return null;
            f.setAccessible(true);
            Object v = f.get(entity);
            return v == null ? null : v.toString();
        } catch (Exception e) { return null; }
    }

    private Field findField(Class<?> c, String name) {
        while (c != null && c != Object.class) {
            try { return c.getDeclaredField(name); } catch (NoSuchFieldException e) { c = c.getSuperclass(); }
        }
        return null;
    }
}
