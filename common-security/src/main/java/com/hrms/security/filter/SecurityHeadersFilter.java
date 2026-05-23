package com.hrms.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Injects OWASP-recommended response headers on every request:
 *
 *   Strict-Transport-Security    — force HTTPS for 1 year, include subdomains
 *   X-Content-Type-Options       — block MIME-sniffing
 *   X-Frame-Options              — clickjacking protection
 *   Referrer-Policy              — strip referrer to cross-origin
 *   Permissions-Policy           — disable surveillance APIs
 *   Content-Security-Policy      — restrict script/style sources
 *   Cross-Origin-Opener-Policy   — process isolation
 *   Cache-Control                — never cache API responses
 *
 * CSP defaults are conservative for an API (no inline JS) — the SPA's gateway should
 * relax CSP for /assets/** via a route-specific filter.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Value("${hrms.security.headers.hsts-max-age:31536000}")
    private long hstsMaxAge;
    @Value("${hrms.security.headers.csp:default-src 'self'; frame-ancestors 'none'}")
    private String csp;
    @Value("${hrms.security.headers.permissions-policy:geolocation=(), microphone=(), camera=()}")
    private String permissionsPolicy;
    @Value("${hrms.security.headers.cache-control:no-store, no-cache, must-revalidate, private}")
    private String cacheControl;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        if (!res.containsHeader("Strict-Transport-Security")) {
            res.setHeader("Strict-Transport-Security", "max-age=" + hstsMaxAge + "; includeSubDomains; preload");
        }
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        res.setHeader("Permissions-Policy", permissionsPolicy);
        res.setHeader("Content-Security-Policy", csp);
        res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        res.setHeader("Cross-Origin-Resource-Policy", "same-site");
        if (req.getRequestURI().startsWith("/api/")) {
            res.setHeader("Cache-Control", cacheControl);
            res.setHeader("Pragma", "no-cache");
        }
        chain.doFilter(req, res);
    }
}
