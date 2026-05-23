package com.hrms.events.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Wiring for Kafka resilience:
 *   1. Exponential back-off retry on listener exceptions (1s → 2s → 4s → 8s, capped 30s, 5 tries).
 *   2. Records that exhaust retries are routed to {@code <topic>.DLQ} preserving partition and key.
 *   3. Non-retryable failures (deserialization, NPE in payload) skip retries and go straight to DLQ.
 *
 * Consumers don't need to do anything — Spring picks this DefaultErrorHandler up as the
 * single registered ErrorHandler bean.
 */
@Slf4j
@Configuration
@ConditionalOnClass(KafkaTemplate.class)
@RequiredArgsConstructor
public class KafkaResilienceConfig {

    public static final String DLQ_SUFFIX = ".DLQ";

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> {
                    log.warn("Routing failed record from {} to DLQ: {}", record.topic(), ex.getMessage());
                    return new org.apache.kafka.common.TopicPartition(
                            record.topic() + DLQ_SUFFIX, record.partition());
                });

        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxInterval(30_000L);
        backOff.setMaxElapsedTime(60_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        // Don't retry obvious "poison pill" errors — straight to DLQ
        handler.addNotRetryableExceptions(
                org.springframework.kafka.support.serializer.DeserializationException.class,
                IllegalArgumentException.class,
                NullPointerException.class,
                ClassCastException.class
        );
        // Always observe send attempts (metrics + logs)
        handler.setRetryListeners((record, ex, attempt) ->
                log.debug("Kafka listener retry {} for {} due to {}", attempt, record.topic(), ex.getMessage()));
        return handler;
    }

    /** Helper for producers to write to DLQ explicitly when business logic decides the message is bad. */
    public static <K, V> void sendToDlq(KafkaTemplate<K, V> template, ProducerRecord<K, V> original, Throwable cause) {
        ProducerRecord<K, V> dlq = new ProducerRecord<>(original.topic() + DLQ_SUFFIX, original.partition(),
                original.timestamp(), original.key(), original.value(), original.headers());
        dlq.headers().add("x-original-topic", original.topic().getBytes());
        if (cause != null) dlq.headers().add("x-error-reason", cause.getMessage().getBytes());
        template.send(dlq);
    }
}
