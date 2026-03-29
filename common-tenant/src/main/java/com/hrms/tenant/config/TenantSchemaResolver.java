package com.hrms.tenant.config;

import com.hrms.security.model.TenantContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TenantSchemaResolver implements CurrentTenantIdentifierResolver<String>,
        HibernatePropertiesCustomizer {
    @Override public String resolveCurrentTenantIdentifier() {
        String t = TenantContext.get(); return (t != null) ? "tenant_" + t : "public";
    }
    @Override public boolean validateExistingCurrentSessions() { return true; }
    @Override public void customize(Map<String, Object> p) {
        p.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
