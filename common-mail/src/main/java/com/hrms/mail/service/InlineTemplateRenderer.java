package com.hrms.mail.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default fallback renderer — supports mustache-style {{key}} and {{key.subkey}} substitution.
 * Services that provide TemplateRenderer beans (e.g. service-notification with DB-backed templates
 * + Thymeleaf) will override this via @ConditionalOnMissingBean.
 */
@Component
@ConditionalOnMissingBean(TemplateRenderer.class)
public class InlineTemplateRenderer implements TemplateRenderer {

    private static final Pattern P = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*}}");

    @Override
    public String renderInline(String template, Map<String, Object> vars) {
        if (template == null) return "";
        if (vars == null || vars.isEmpty()) return template;
        Matcher m = P.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String key = m.group(1);
            Object val = resolve(vars, key);
            m.appendReplacement(sb, Matcher.quoteReplacement(val == null ? "" : val.toString()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Override
    public String renderByCode(String tenantId, String templateCode, String locale, Map<String, Object> vars) {
        throw new UnsupportedOperationException(
                "DB-backed template lookup must be provided by service-notification or similar.");
    }

    @SuppressWarnings("unchecked")
    private Object resolve(Map<String, Object> vars, String dottedKey) {
        String[] parts = dottedKey.split("\\.");
        Object cur = vars;
        for (String p : parts) {
            if (cur instanceof Map) cur = ((Map<String, Object>) cur).get(p);
            else return null;
            if (cur == null) return null;
        }
        return cur;
    }
}
