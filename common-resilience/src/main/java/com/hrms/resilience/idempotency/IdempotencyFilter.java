package com.hrms.resilience.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Honours Stripe-style {@code Idempotency-Key} header on POST/PUT/PATCH/DELETE requests.
 * First call executes and caches (status + body); replays return the cached response
 * verbatim. Cache lives in Redis for 24h by default so a re-tried mobile-network request
 * won't double-create a payroll run.
 *
 * Key namespace: idempotency:{tenant}:{user}:{key}
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
@ConditionalOnProperty(name = "hrms.resilience.idempotency.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String HEADER = "Idempotency-Key";

    @Value("${hrms.resilience.idempotency.ttl-seconds:86400}")
    private long ttlSeconds;

    private final StringRedisTemplate redis;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String method = req.getMethod();
        if (!"POST".equals(method) && !"PUT".equals(method) && !"PATCH".equals(method) && !"DELETE".equals(method)) {
            chain.doFilter(req, res);
            return;
        }
        String key = req.getHeader(HEADER);
        if (key == null || key.isBlank()) {
            chain.doFilter(req, res);
            return;
        }
        String tenant = req.getHeader("X-Tenant-Id");
        String user = req.getHeader("X-User-Id");
        String cacheKey = "idempotency:" + (tenant == null ? "_" : tenant) + ":"
                + (user == null ? "_" : user) + ":" + key;

        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            int idx = cached.indexOf('\n');
            int status = idx > 0 ? Integer.parseInt(cached.substring(0, idx)) : 200;
            String body = idx > 0 ? cached.substring(idx + 1) : cached;
            res.setStatus(status);
            res.setHeader("Idempotent-Replayed", "true");
            res.setContentType("application/json");
            res.getWriter().write(body);
            return;
        }

        CachingResponseWrapper wrapper = new CachingResponseWrapper(res);
        chain.doFilter(req, wrapper);
        wrapper.flushBuffer();
        if (wrapper.getStatus() >= 200 && wrapper.getStatus() < 300) {
            redis.opsForValue().set(cacheKey,
                    wrapper.getStatus() + "\n" + wrapper.capturedBody(),
                    Duration.ofSeconds(ttlSeconds));
        }
    }

    private static class CachingResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        private final PrintWriter writer = new PrintWriter(new OutputStreamWriter(buf, StandardCharsets.UTF_8));
        private final jakarta.servlet.ServletOutputStream out = new jakarta.servlet.ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(jakarta.servlet.WriteListener l) { }
            @Override public void write(int b) { buf.write(b); }
        };
        CachingResponseWrapper(HttpServletResponse r) { super(r); }
        @Override public PrintWriter getWriter() { return writer; }
        @Override public jakarta.servlet.ServletOutputStream getOutputStream() { return out; }
        @Override public void flushBuffer() throws IOException {
            writer.flush();
            byte[] body = buf.toByteArray();
            ((HttpServletResponse) getResponse()).getOutputStream().write(body);
        }
        String capturedBody() { writer.flush(); return buf.toString(StandardCharsets.UTF_8); }
    }
}
