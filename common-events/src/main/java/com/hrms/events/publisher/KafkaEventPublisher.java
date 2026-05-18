package com.hrms.events.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.events.model.DomainEvent;
import com.hrms.events.outbox.OutboxEvent;
import com.hrms.events.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final OutboxRepository outboxRepo;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    @Override
    public void publish(String topic, DomainEvent event) {
        try {
            outboxRepo.save(OutboxEvent.builder()
                    .topic(topic).eventId(event.getEventId()).eventType(event.getEventType())
                    .tenantId(event.getTenantId()).aggregateId(event.getAggregateId())
                    .payload(mapper.writeValueAsString(event))
                    .createdAt(OffsetDateTime.now()).attempts(0).build());
        } catch (Exception e) {
            throw new RuntimeException("Outbox write failed", e);
        }
    }

    @Override
    public void publishDirect(String topic, DomainEvent event) {
        try {
            kafka.send(topic, event.getAggregateId(), mapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new RuntimeException("Kafka direct send failed", e);
        }
    }
}
