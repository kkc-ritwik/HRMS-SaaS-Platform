package com.hrms.notification.dispatch.channels;

import com.hrms.notification.dispatch.DispatchRequest;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlackChannel {

    @Value("${hrms.notification.slack.bot-token:}") private String botToken;

    public void send(DispatchRequest req) {
        if (botToken == null || botToken.isBlank() || botToken.startsWith("REPLACE")) {
            log.warn("Slack channel not configured — message skipped.");
            return;
        }
        if (req.getSlackChannel() == null) return;
        try {
            MethodsClient client = Slack.getInstance().methods(botToken);
            client.chatPostMessage(ChatPostMessageRequest.builder()
                    .channel(req.getSlackChannel())
                    .text((req.getSubject() == null ? "" : "*" + req.getSubject() + "*\n") +
                          (req.getBody() == null ? "" : req.getBody()))
                    .build());
        } catch (Exception e) {
            log.error("Slack send failed: {}", e.getMessage());
        }
    }
}
