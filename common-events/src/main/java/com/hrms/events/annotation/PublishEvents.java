package com.hrms.events.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark an entity to auto-publish lifecycle events (created/updated/deleted) to Kafka.
 * Apply alongside {@code @EntityListeners(EntityLifecyclePublisher.class)}.
 *
 * Topic + event type are derived from the entity simple name; override via attributes.
 *
 * Example:
 *   @PublishEvents(topic = Topics.EMPLOYEE, namespace = "employee")
 *   @EntityListeners({AuditEntityListener.class, EntityLifecyclePublisher.class})
 *   public class Employee extends BaseEntity { ... }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PublishEvents {

    /** Kafka topic (use values from {@link com.hrms.events.model.Topics}). */
    String topic();

    /**
     * Event-type prefix. e.g. namespace="employee" → emits "employee.created", "employee.updated",
     * "employee.deleted". Defaults to lower-cased entity simple name.
     */
    String namespace() default "";

    /** If true, the entity's `id` field is used as the aggregate key. */
    boolean useIdAsAggregateKey() default true;

    /** Skip the UPDATE event (some entities update too frequently to be worth audit/event noise). */
    boolean skipUpdates() default false;
}
