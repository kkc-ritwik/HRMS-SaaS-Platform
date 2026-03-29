package com.hrms.security.service;

import com.hrms.security.model.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtService {
    @Value("${jwt.secret:hrms-platform-jwt-secret-key-change-in-prod-256bits!!}")
    private String secret;
    @Value("${jwt.access-token-expiry:3600000}") private long accessExpiry;
    @Value("${jwt.refresh-token-expiry:604800000}") private long refreshExpiry;

    private SecretKey key() { return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }

    public String generateAccessToken(UserPrincipal user) {
        return Jwts.builder().subject(user.getId())
                .claim("tenantId", user.getTenantId()).claim("email", user.getEmail())
                .claim("name", user.getFullName()).claim("employeeId", user.getEmployeeId())
                .claim("roles", user.getRoles()).claim("permissions", user.getPermissions())
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + accessExpiry))
                .signWith(key()).compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder().subject(userId).claim("type", "refresh")
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + refreshExpiry))
                .signWith(key()).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }
    public boolean isValid(String token) { try { parse(token); return true; } catch (Exception e) { return false; } }
    public String getUserId(String token) { return parse(token).getSubject(); }
    public String getTenantId(String token) { return parse(token).get("tenantId", String.class); }
}
