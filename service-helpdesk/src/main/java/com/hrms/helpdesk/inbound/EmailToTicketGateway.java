package com.hrms.helpdesk.inbound;

import com.hrms.helpdesk.entity.Ticket;
import com.hrms.helpdesk.repository.TicketRepository;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Properties;

/**
 * IMAP poller that turns inbound emails into tickets. Polls UNREAD messages every
 * {@code hrms.helpdesk.inbound.poll-ms} (default 60s), creates a Ticket per message,
 * marks the source mail as SEEN, and (optionally) deletes after processing.
 *
 * Configure with hrms.helpdesk.inbound.* in application.yml. Disabled by default
 * (set enabled=true to activate). Works with Gmail (IMAP+app password) and any
 * standard IMAP-S server.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hrms.helpdesk.inbound.enabled", havingValue = "true")
public class EmailToTicketGateway {

    private final TicketRepository tickets;

    @Value("${hrms.helpdesk.inbound.host:imap.gmail.com}")  private String host;
    @Value("${hrms.helpdesk.inbound.port:993}")              private int port;
    @Value("${hrms.helpdesk.inbound.username:}")             private String username;
    @Value("${hrms.helpdesk.inbound.password:}")             private String password;
    @Value("${hrms.helpdesk.inbound.folder:INBOX}")          private String folder;
    @Value("${hrms.helpdesk.inbound.delete-after-process:false}") private boolean deleteAfter;
    @Value("${hrms.helpdesk.inbound.tenant-id:default}")     private String tenantId;
    @Value("${hrms.helpdesk.inbound.fallback-requester-id:}") private String fallbackRequesterId;

    @Scheduled(fixedDelayString = "${hrms.helpdesk.inbound.poll-ms:60000}")
    @Transactional
    public void pollInbox() {
        if (username == null || username.isBlank()) return;
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", host);
        props.setProperty("mail.imaps.port", String.valueOf(port));
        props.setProperty("mail.imaps.ssl.enable", "true");

        Session session = Session.getInstance(props);
        try (Store store = session.getStore("imaps")) {
            store.connect(host, port, username, password);
            Folder inbox = store.getFolder(folder);
            inbox.open(deleteAfter ? Folder.READ_WRITE : Folder.READ_WRITE);
            Message[] unread = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            for (Message m : unread) {
                try {
                    processMessage(m);
                    m.setFlag(Flags.Flag.SEEN, true);
                    if (deleteAfter) m.setFlag(Flags.Flag.DELETED, true);
                } catch (Exception e) {
                    log.warn("Failed to process inbound message: {}", e.getMessage());
                }
            }
            inbox.close(deleteAfter);
        } catch (Exception e) {
            log.error("IMAP poll failed: {}", e.getMessage());
        }
    }

    private void processMessage(Message m) throws MessagingException, IOException {
        String subject = m.getSubject() == null ? "(no subject)" : m.getSubject();
        Address[] from = m.getFrom();
        String fromEmail = (from != null && from.length > 0 && from[0] instanceof InternetAddress ia)
                ? ia.getAddress() : "unknown@unknown";
        String body = extractBody(m);

        Ticket t = new Ticket();
        t.setTenantId(tenantId);
        t.setTitle(subject);
        t.setDescription("From: " + fromEmail + "\n\n" + body);
        if (fallbackRequesterId != null && !fallbackRequesterId.isBlank()) {
            try { t.setRequesterId(java.util.UUID.fromString(fallbackRequesterId)); }
            catch (IllegalArgumentException ignored) {}
        }
        t.setPriority(Ticket.Priority.MEDIUM);
        t.setStatus(Ticket.TicketStatus.OPEN);
        tickets.save(t);
        log.info("Inbound email → ticket created. subject='{}' from={}", subject, fromEmail);
    }

    private String extractBody(Message m) throws MessagingException, IOException {
        Object content = m.getContent();
        if (content instanceof String s) return s;
        if (content instanceof Multipart mp) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) sb.append(bp.getContent());
            }
            if (sb.length() > 0) return sb.toString();
        }
        return "(unable to extract body)";
    }
}
