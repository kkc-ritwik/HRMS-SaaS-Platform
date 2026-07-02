package com.hrms.security.config;

import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/**
 * Delegating method-security expression root that treats SUPER_ADMIN as a superuser: every
 * {@code hasAuthority}/{@code hasRole} check short-circuits to true. Everything else is forwarded
 * to the real Spring root.
 *
 * Implemented as a wrapper (rather than a subclass of SecurityExpressionRoot) because the
 * {@code hasAuthority}/{@code hasRole} methods there are {@code final} and cannot be overridden.
 * This avoids having to seed SUPER_ADMIN with the full, ever-growing set of {@code RESOURCE:ACTION}
 * permissions that {@code @PreAuthorize} expressions check across all services.
 */
public class SuperAdminExpressionRoot implements MethodSecurityExpressionOperations {

    private final MethodSecurityExpressionOperations delegate;

    public SuperAdminExpressionRoot(MethodSecurityExpressionOperations delegate) {
        this.delegate = delegate;
    }

    private boolean isSuperAdmin() {
        Authentication a = delegate.getAuthentication();
        return a != null && a.getAuthorities().stream()
                .anyMatch(g -> "ROLE_SUPER_ADMIN".equals(g.getAuthority()));
    }

    @Override public boolean hasAuthority(String authority) { return isSuperAdmin() || delegate.hasAuthority(authority); }
    @Override public boolean hasAnyAuthority(String... authorities) { return isSuperAdmin() || delegate.hasAnyAuthority(authorities); }
    @Override public boolean hasRole(String role) { return isSuperAdmin() || delegate.hasRole(role); }
    @Override public boolean hasAnyRole(String... roles) { return isSuperAdmin() || delegate.hasAnyRole(roles); }
    @Override public boolean hasPermission(Object target, Object permission) { return isSuperAdmin() || delegate.hasPermission(target, permission); }
    @Override public boolean hasPermission(Object targetId, String targetType, Object permission) { return isSuperAdmin() || delegate.hasPermission(targetId, targetType, permission); }

    @Override public Authentication getAuthentication() { return delegate.getAuthentication(); }
    @Override public boolean permitAll() { return delegate.permitAll(); }
    @Override public boolean denyAll() { return delegate.denyAll(); }
    @Override public boolean isAnonymous() { return delegate.isAnonymous(); }
    @Override public boolean isAuthenticated() { return delegate.isAuthenticated(); }
    @Override public boolean isRememberMe() { return delegate.isRememberMe(); }
    @Override public boolean isFullyAuthenticated() { return delegate.isFullyAuthenticated(); }

    @Override public void setFilterObject(Object filterObject) { delegate.setFilterObject(filterObject); }
    @Override public Object getFilterObject() { return delegate.getFilterObject(); }
    @Override public void setReturnObject(Object returnObject) { delegate.setReturnObject(returnObject); }
    @Override public Object getReturnObject() { return delegate.getReturnObject(); }
    @Override public Object getThis() { return delegate.getThis(); }
}
