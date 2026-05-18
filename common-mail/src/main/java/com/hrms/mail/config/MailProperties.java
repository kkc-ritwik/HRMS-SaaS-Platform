package com.hrms.mail.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hrms.mail")
public class MailProperties {
    private boolean enabled = true;
    /** "noreply@yourcompany.com" — must be a verified sender on your SMTP provider. */
    private String fromAddress = "noreply@hrms.local";
    private String fromName = "HRMS";
    /** Optional reply-to address. */
    private String replyTo;
    /** True = send asynchronously via @Async thread pool. */
    private boolean asyncSend = true;
    /** Number of retry attempts on transient failure. */
    private int retryAttempts = 3;
    private long retryBackoffMs = 2000L;
    /** Optional BCC for all outgoing mail (compliance / audit). */
    private String bccAuditAddress;
    /** Cap on number of recipients per send (anti-blast safety). */
    private int maxRecipients = 100;
}
