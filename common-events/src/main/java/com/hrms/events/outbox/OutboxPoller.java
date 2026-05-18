package com.hrms.events.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BATCH = 50;

    private final OutboxRepository repo;
    private final KafkaTemplate<String, String> kafka;

    @Scheduled(fixedDelayString = "${hrms.events.outbox.poll-ms:2000}")
    @Transactional
    public void drain() {
        List<OutboxEvent> batch = repo.findUnsent(MAX_ATTEMPTS, PageRequest.of(0, BATCH));
        for (OutboxEvent e : batch) {
            try {
                kafka.send(e.getTopic(), e.getAggregateId(), e.getPayload()).get();
                e.setSentAt(OffsetDateTime.now());
            } catch (Exception ex) {
                e.setAttempts(e.getAttempts() + 1);
                e.setLastError(ex.getMessage());
                log.warn("Outbox send failed for {}: {}", e.getEventId(), ex.getMessage());
            }
            repo.save(e);
        }
    }
}
