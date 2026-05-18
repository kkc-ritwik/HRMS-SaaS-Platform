package com.hrms.security.filter;

import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * After JwtAuthFilter has set the SecurityContext, this filter populates TenantContext
 * and the AuditContext (if common-audit is on the classpath) for downstream listeners.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain)
            throws ServletException, IOException {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a != null && a.getPrincipal() instanceof UserPrincipal up) {
                TenantContext.set(up.getTenantId());
                try {
                    Class<?> ctxCls = Class.forName("com.hrms.audit.context.AuditContext");
                    Class<?> snapCls = Class.forName("com.hrms.audit.context.AuditContext$Snapshot");
                    Object snap = snapCls.getConstructors()[0].newInstance(
                            up.getId(), up.getEmail(), req.getRemoteAddr(),
                            req.getHeader("User-Agent"), req.getHeader("X-Request-Id"));
                    ctxCls.getMethod("set", snapCls).invoke(null, snap);
                } catch (ClassNotFoundException ignored) {
                    // common-audit not on classpath in this service — skip
                } catch (Exception ignored) { /* defensive: never block the request */ }
            } else {
                // Fallback: header-driven tenant context for inter-service / API-key calls
                String t = req.getHeader("X-Tenant-Id");
                if (t != null && !t.isBlank()) TenantContext.set(t);
            }
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
            try {
                Class<?> ctxCls = Class.forName("com.hrms.audit.context.AuditContext");
                ctxCls.getMethod("clear").invoke(null);
            } catch (Exception ignored) {}
        }
    }
}
