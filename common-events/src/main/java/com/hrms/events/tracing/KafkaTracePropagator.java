package com.hrms.events.tracing;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Kafka producer + consumer interceptors that copy trace context (traceId, tenantId,
 * userId, requestId) onto headers when producing, and lift them back into MDC when
 * consuming. Without this, an event-driven request loses correlation as soon as it
 * crosses a Kafka boundary.
 *
 * Wire into a service's application.yml:
 *   spring.kafka.producer.properties:
 *     interceptor.classes: com.hrms.events.tracing.KafkaTracePropagator$ProducerSide
 *   spring.kafka.consumer.properties:
 *     interceptor.classes: com.hrms.events.tracing.KafkaTracePropagator$ConsumerSide
 */
public class KafkaTracePropagator {

    public static final String H_TRACE  = "x-trace-id";
    public static final String H_TENANT = "x-tenant-id";
    public static final String H_USER   = "x-user-id";
    public static final String H_REQ    = "x-request-id";

    public static class ProducerSide<K, V> implements ProducerInterceptor<K, V> {
        @Override public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
            addIfPresent(record, H_TRACE, MDC.get("traceId"));
            addIfPresent(record, H_TENANT, MDC.get("tenantId"));
            addIfPresent(record, H_USER, MDC.get("userId"));
            addIfPresent(record, H_REQ, MDC.get("requestId"));
            return record;
        }
        @Override public void onAcknowledgement(RecordMetadata metadata, Exception exception) {}
        @Override public void close() {}
        @Override public void configure(Map<String, ?> configs) {}

        private static void addIfPresent(ProducerRecord<?, ?> record, String name, String value) {
            if (value == null || record.headers().lastHeader(name) != null) return;
            record.headers().add(name, value.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static class ConsumerSide<K, V> implements ConsumerInterceptor<K, V> {
        @Override public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {
            for (ConsumerRecord<K, V> r : records) {
                lift(r, H_TRACE, "traceId");
                lift(r, H_TENANT, "tenantId");
                lift(r, H_USER, "userId");
                lift(r, H_REQ, "requestId");
            }
            return records;
        }
        @Override public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {}
        @Override public void close() {}
        @Override public void configure(Map<String, ?> configs) {}

        private static void lift(ConsumerRecord<?, ?> r, String headerName, String mdcKey) {
            Header h = r.headers().lastHeader(headerName);
            if (h != null && h.value() != null) {
                MDC.put(mdcKey, new String(h.value(), StandardCharsets.UTF_8));
            }
        }
    }
}
