package com.hrms.resilience.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hrms.resilience.ratelimit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimiter limiter;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${hrms.resilience.ratelimit.requests-per-minute:300}") private int rpm;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String key = bucketKey(req);
        if (!limiter.allow(key, rpm, Duration.ofMinutes(1))) {
            res.setStatus(429);
            res.setContentType("application/json");
            res.setHeader("Retry-After", "60");
            mapper.writeValue(res.getWriter(), Map.of(
                    "status", 429, "error", "Too Many Requests",
                    "message", "Rate limit exceeded — " + rpm + " req/min"));
            return;
        }
        chain.doFilter(req, res);
    }

    private String bucketKey(HttpServletRequest req) {
        String tenant = req.getHeader("X-Tenant-Id");
        String user = req.getHeader("X-User-Id");
        if (user != null) return "u:" + (tenant == null ? "_" : tenant) + ":" + user;
        return "ip:" + req.getRemoteAddr();
    }
}
