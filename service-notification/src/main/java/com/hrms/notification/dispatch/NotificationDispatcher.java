package com.hrms.notification.dispatch;

import com.hrms.mail.model.MailRequest;
import com.hrms.mail.service.MailService;
import com.hrms.notification.dispatch.channels.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Routes a unified DispatchRequest to the appropriate channel adapters (mail, SMS, push, Slack, etc.).
 * Each adapter is no-op if its provider creds are missing — soft failure with a WARN.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final MailService mailService;
    private final SmsChannel smsChannel;
    private final PushChannel pushChannel;
    private final SlackChannel slackChannel;
    private final TeamsChannel teamsChannel;
    private final WhatsappChannel whatsappChannel;
    private final InAppChannel inAppChannel;

    public void dispatch(DispatchRequest req) {
        if (req.getChannels() == null || req.getChannels().isEmpty()) {
            log.warn("DispatchRequest with no channels — dropping. category={}", req.getCategory());
            return;
        }
        for (Channel c : req.getChannels()) {
            try {
                switch (c) {
                    case EMAIL    -> dispatchEmail(req);
                    case SMS      -> smsChannel.send(req);
                    case PUSH     -> pushChannel.send(req);
                    case SLACK    -> slackChannel.send(req);
                    case TEAMS    -> teamsChannel.send(req);
                    case WHATSAPP -> whatsappChannel.send(req);
                    case IN_APP   -> inAppChannel.send(req);
                }
            } catch (Exception e) {
                log.error("Channel {} dispatch failed: {}", c, e.getMessage(), e);
            }
        }
    }

    private void dispatchEmail(DispatchRequest req) {
        if (req.getToEmails() == null || req.getToEmails().isEmpty()) return;
        mailService.sendAsync(MailRequest.builder()
                .to(req.getToEmails())
                .subject(req.getSubject())
                .html(req.getHtml())
                .text(req.getBody())
                .templateCode(req.getTemplateCode())
                .variables(req.getVariables() == null ? java.util.Map.of() : req.getVariables())
                .tenantId(req.getTenantId())
                .category(req.getCategory())
                .locale(req.getLocale())
                .build());
    }

    public List<Channel> allChannels() { return List.of(Channel.values()); }
}
