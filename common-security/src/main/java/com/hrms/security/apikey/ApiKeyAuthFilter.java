package com.hrms.security.apikey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lightweight API-key auth for inbound webhooks / integration partners.
 * Keys live in env (HRMS_API_KEYS=key1,key2). Real prod should look these up in DB.
 */
@Component
@ConditionalOnProperty(name = "hrms.security.api-key.enabled", havingValue = "true")
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    @Value("${hrms.security.api-key.keys:}")
    private String csvKeys;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String k = req.getHeader(HEADER);
        if (k != null && !k.isBlank() && csvKeys != null && !csvKeys.isBlank()) {
            for (String allowed : csvKeys.split(",")) {
                if (allowed.trim().equals(k)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken("api-key-client", null,
                                    List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT")));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    break;
                }
            }
        }
        chain.doFilter(req, res);
    }
}
