package com.hrms.notification.dispatch.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.notification.dispatch.DispatchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamsChannel {

    @Value("${hrms.notification.teams.default-webhook:}") private String defaultWebhook;
    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate rest = new RestTemplate();

    public void send(DispatchRequest req) {
        String webhook = req.getTeamsWebhookUrl() != null ? req.getTeamsWebhookUrl() : defaultWebhook;
        if (webhook == null || webhook.isBlank() || webhook.startsWith("REPLACE")) {
            log.warn("MS Teams webhook not configured — message skipped.");
            return;
        }
        try {
            Map<String, Object> card = Map.of(
                    "@type", "MessageCard", "@context", "https://schema.org/extensions",
                    "summary", req.getSubject() == null ? "HRMS Notification" : req.getSubject(),
                    "themeColor", "0078D7",
                    "title", req.getSubject() == null ? "HRMS" : req.getSubject(),
                    "text", req.getBody() == null ? (req.getHtml() == null ? "" : req.getHtml()) : req.getBody());
            HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
            rest.postForEntity(webhook, new HttpEntity<>(mapper.writeValueAsString(card), h), String.class);
        } catch (Exception e) {
            log.error("Teams send failed: {}", e.getMessage());
        }
    }
}
