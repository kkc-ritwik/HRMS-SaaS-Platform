package com.hrms.mail.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MailRequest {
    @Singular("to") private List<String> to;
    @Singular("cc") private List<String> cc;
    @Singular("bcc") private List<String> bcc;
    private String subject;
    /** Either provide rendered html OR templateCode + variables (template wins if both). */
    private String html;
    private String text;
    private String templateCode;
    @Singular("var") private Map<String, Object> variables;
    @Singular private List<MailAttachment> attachments;
    /** Optional tenant context for audit / template lookup. */
    private String tenantId;
    /** Optional category for analytics (e.g. "payslip", "leave-approval"). */
    private String category;
    private String locale;
}
