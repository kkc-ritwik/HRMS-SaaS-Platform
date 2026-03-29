package com.hrms.security.filter;

import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import com.hrms.security.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.*;

@Component @RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain)
            throws ServletException, IOException {
        try {
            String h = req.getHeader("Authorization");
            if (h != null && h.startsWith("Bearer ")) {
                String token = h.substring(7);
                if (jwtService.isValid(token)) {
                    Claims c = jwtService.parse(token);
                    @SuppressWarnings("unchecked") List<String> roles = c.get("roles", List.class);
                    @SuppressWarnings("unchecked") List<String> perms = c.get("permissions", List.class);
                    UserPrincipal p = UserPrincipal.builder().id(c.getSubject())
                            .tenantId(c.get("tenantId", String.class)).email(c.get("email", String.class))
                            .fullName(c.get("name", String.class)).employeeId(c.get("employeeId", String.class))
                            .roles(roles != null ? new HashSet<>(roles) : Set.of())
                            .permissions(perms != null ? new HashSet<>(perms) : Set.of()).build();
                    TenantContext.set(p.getTenantId());
                    var auth = new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            String th = req.getHeader("X-Tenant-ID");
            if (th != null && TenantContext.get() == null) TenantContext.set(th);
            chain.doFilter(req, res);
        } finally { TenantContext.clear(); }
    }
}
