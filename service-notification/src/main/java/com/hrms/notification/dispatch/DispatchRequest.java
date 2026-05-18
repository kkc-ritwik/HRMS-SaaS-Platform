package com.hrms.notification.dispatch;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DispatchRequest {
    private String tenantId;
    private List<String> toEmails;
    private List<String> toPhones;
    private List<String> toFcmTokens;
    private List<String> toUserIds;          // for IN_APP / WebSocket
    private String slackChannel;             // e.g. "#hr-announcements" or "U02ABCXYZ"
    private String teamsWebhookUrl;
    private List<Channel> channels;
    private String subject;
    private String body;                     // plain text fallback
    private String html;
    private String templateCode;             // resolves to DB-backed template if present
    private Map<String, Object> variables;
    private String locale;
    private String category;                 // e.g. "leave.approved"
    private String referenceType;
    private String referenceId;
    private String priority;                 // LOW|NORMAL|HIGH|URGENT
}
