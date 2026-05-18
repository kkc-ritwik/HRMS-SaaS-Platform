package com.hrms.notification.template;

import com.hrms.mail.service.TemplateRenderer;
import com.hrms.notification.entity.EmailTemplate;
import com.hrms.notification.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Primary template renderer for service-notification — overrides the inline-only fallback
 * from common-mail. Looks up EmailTemplate by tenant + code, then substitutes mustache
 * placeholders. Falls back to the inline {@code {{key}}} engine for ad-hoc templates.
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class DbBackedTemplateRenderer implements TemplateRenderer {

    private static final Pattern P = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*}}");

    private final EmailTemplateRepository templates;

    @Override
    public String renderInline(String template, Map<String, Object> vars) {
        return substitute(template, vars);
    }

    @Override
    public String renderByCode(String tenantId, String templateCode, String locale, Map<String, Object> vars) {
        EmailTemplate t = findTemplate(tenantId, templateCode);
        if (t == null) {
            log.warn("Email template '{}' not found for tenant '{}' — using empty body", templateCode, tenantId);
            return "";
        }
        return substitute(t.getBodyHtml(), vars);
    }

    /** Optional helper for callers that also want the resolved subject. */
    public ResolvedTemplate resolve(String tenantId, String templateCode, Map<String, Object> vars) {
        EmailTemplate t = findTemplate(tenantId, templateCode);
        if (t == null) return new ResolvedTemplate("", "", "");
        return new ResolvedTemplate(
                substitute(t.getSubject(), vars),
                substitute(t.getBodyHtml(), vars),
                substitute(t.getBodyText(), vars));
    }

    private EmailTemplate findTemplate(String tenantId, String code) {
        // First try tenant-specific override, then fall back to system-wide template ("default" tenant).
        for (String tid : new String[]{tenantId, "default", "system"}) {
            if (tid == null) continue;
            for (EmailTemplate t : templates.findAll()) {
                if (code.equals(t.getCode())
                        && (tid.equals(t.getTenantId()) || t.getTenantId() == null)
                        && t.isActive()) return t;
            }
        }
        return null;
    }

    private String substitute(String template, Map<String, Object> vars) {
        if (template == null) return "";
        if (vars == null || vars.isEmpty()) return template;
        Matcher m = P.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Object v = resolveDotted(vars, m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(v == null ? "" : v.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Object resolveDotted(Map<String, Object> vars, String dottedKey) {
        Object cur = vars;
        for (String p : dottedKey.split("\\.")) {
            if (cur instanceof Map) cur = ((Map<String, Object>) cur).get(p);
            else return null;
            if (cur == null) return null;
        }
        return cur;
    }

    public record ResolvedTemplate(String subject, String html, String text) {}
}
