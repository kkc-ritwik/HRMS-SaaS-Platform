package com.hrms.auth.security;

import com.hrms.auth.repository.TenantIpAllowlistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

/**
 * Rejects login attempts originating from IPs outside the tenant's allowlist.
 * Tenant is resolved from the X-Tenant-Id header (or query param fallback).
 * Allowlist entries are CIDR ranges; an empty list means "allow all".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpAllowlistFilter extends OncePerRequestFilter {

    private final TenantIpAllowlistRepository repo;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res,
                                     @NonNull FilterChain chain) throws ServletException, IOException {
        if (req.getRequestURI() == null || !req.getRequestURI().startsWith("/api/v1/auth/login")) {
            chain.doFilter(req, res); return;
        }
        String tenant = req.getHeader("X-Tenant-Id");
        if (tenant == null) { chain.doFilter(req, res); return; }
        List<TenantIpAllowlist> rules = repo.findByTenantIdAndActiveTrue(tenant).stream()
                .filter(r -> r.getAppliesTo() == TenantIpAllowlist.AppliesTo.LOGIN
                        || r.getAppliesTo() == TenantIpAllowlist.AppliesTo.BOTH).toList();
        if (rules.isEmpty()) { chain.doFilter(req, res); return; }

        String clientIp = clientIp(req);
        boolean allowed = rules.stream().anyMatch(r -> matches(r.getCidr(), clientIp));
        if (!allowed) {
            log.warn("Login rejected — IP {} not in allowlist for tenant {}", clientIp, tenant);
            res.setStatus(403);
            res.getWriter().write("{\"error\":\"IP_NOT_ALLOWED\",\"message\":\"This IP is not permitted to login\"}");
            return;
        }
        chain.doFilter(req, res);
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    /** Trivial CIDR matcher (IPv4). For production-grade matching, use commons-net SubnetUtils. */
    private boolean matches(String cidr, String ip) {
        if (cidr == null || ip == null) return false;
        try {
            if (!cidr.contains("/")) return cidr.equals(ip);
            String[] parts = cidr.split("/");
            int prefix = Integer.parseInt(parts[1]);
            byte[] netBytes = InetAddress.getByName(parts[0]).getAddress();
            byte[] ipBytes = InetAddress.getByName(ip).getAddress();
            if (netBytes.length != ipBytes.length) return false;
            int fullBytes = prefix / 8;
            int remBits = prefix % 8;
            for (int i = 0; i < fullBytes; i++) if (netBytes[i] != ipBytes[i]) return false;
            if (remBits == 0) return true;
            int mask = 0xff << (8 - remBits) & 0xff;
            return (netBytes[fullBytes] & mask) == (ipBytes[fullBytes] & mask);
        } catch (Exception e) { return false; }
    }
}
