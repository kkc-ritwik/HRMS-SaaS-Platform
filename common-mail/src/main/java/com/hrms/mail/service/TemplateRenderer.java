package com.hrms.mail.service;

import java.util.Map;

public interface TemplateRenderer {
    /** Render an inline template string (mustache-style {{vars}}). Cheap, no IO. */
    String renderInline(String template, Map<String, Object> vars);

    /** Lookup template by code from a DB-backed source (provided by the service that owns templates). */
    String renderByCode(String tenantId, String templateCode, String locale, Map<String, Object> vars);
}
