package com.hrms.resilience.http;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Builds a {@link RestClient} pre-wired with circuit-breaker + retry + time-limiter for
 * outbound calls. Each named connector picks up the right Resilience4j configuration
 * profile ({@code outbound} / {@code thirdparty} / {@code db} / {@code kafka}).
 *
 * Usage:
 *   ResilientCall&lt;String&gt; call = httpFactory.named("docusign")
 *       .restClient(builder -&gt; builder.baseUrl("https://docusign.example.com"));
 *   String body = call.execute(rc -&gt; rc.get().uri("/envelopes").retrieve().body(String.class));
 */
@Component
@RequiredArgsConstructor
public class ResilientHttpClientFactory {

    private final CircuitBreakerRegistry breakers;
    private final RetryRegistry retries;
    private final TimeLimiterRegistry timeLimiters;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public NamedConnector named(String name) { return new NamedConnector(name, "outbound"); }

    public NamedConnector thirdparty(String name) { return new NamedConnector(name, "thirdparty"); }

    public class NamedConnector {
        private final String name;
        private final String configKey;

        NamedConnector(String name, String configKey) {
            this.name = name;
            this.configKey = configKey;
        }

        public CircuitBreaker breaker() { return breakers.circuitBreaker(name, configKey); }
        public Retry retry() { return retries.retry(name, "default"); }
        public TimeLimiter timeLimiter() { return timeLimiters.timeLimiter(name, configKey); }

        public RestClient.Builder restClientBuilder() {
            return RestClient.builder()
                    .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                        setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
                        setReadTimeout((int) Duration.ofSeconds(15).toMillis());
                    }});
        }

        /** Decorate any callable with circuit-breaker + retry. */
        public <T> T execute(java.util.function.Supplier<T> body) {
            CircuitBreaker cb = breaker();
            Retry r = retry();
            return Retry.decorateSupplier(r, CircuitBreaker.decorateSupplier(cb, body)).get();
        }
    }

    public ScheduledExecutorService scheduler() { return scheduler; }
}
