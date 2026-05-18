package com.hrms.mail.service;

import com.hrms.mail.config.MailProperties;
import com.hrms.mail.model.MailAttachment;
import com.hrms.mail.model.MailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;
    private final MailProperties props;
    private final TemplateRenderer renderer;

    @Override
    public void send(MailRequest req) {
        if (!props.isEnabled()) {
            log.warn("Mail disabled — would send: subject='{}' to={}", req.getSubject(), req.getTo());
            return;
        }
        validate(req);

        String html = req.getHtml();
        String text = req.getText();
        if (req.getTemplateCode() != null && !req.getTemplateCode().isBlank()) {
            html = renderer.renderByCode(req.getTenantId(), req.getTemplateCode(),
                    req.getLocale(), req.getVariables());
        } else if (html != null) {
            html = renderer.renderInline(html, req.getVariables());
        }
        if (text == null && html != null) {
            text = Jsoup.parse(html).text();
        }

        Throwable last = null;
        for (int i = 0; i < Math.max(1, props.getRetryAttempts()); i++) {
            try {
                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
                h.setFrom(new InternetAddress(props.getFromAddress(), props.getFromName()));
                if (props.getReplyTo() != null) h.setReplyTo(props.getReplyTo());
                if (req.getTo() != null) h.setTo(req.getTo().toArray(String[]::new));
                if (req.getCc() != null && !req.getCc().isEmpty()) h.setCc(req.getCc().toArray(String[]::new));
                if (req.getBcc() != null && !req.getBcc().isEmpty()) h.setBcc(req.getBcc().toArray(String[]::new));
                if (props.getBccAuditAddress() != null) {
                    h.addBcc(props.getBccAuditAddress());
                }
                h.setSubject(req.getSubject() == null ? "" : req.getSubject());
                if (html != null) h.setText(text == null ? "" : text, html);
                else h.setText(text == null ? "" : text);

                if (req.getAttachments() != null) {
                    for (MailAttachment a : req.getAttachments()) {
                        if (a.getContent() != null) {
                            h.addAttachment(a.getFilename(),
                                    new ByteArrayResource(a.getContent()),
                                    a.getContentType() == null ? "application/octet-stream" : a.getContentType());
                        }
                    }
                }
                mailSender.send(msg);
                log.info("Mail sent: to={} subject='{}' category={}", req.getTo(), req.getSubject(), req.getCategory());
                return;
            } catch (MessagingException | UnsupportedEncodingException | org.springframework.mail.MailException e) {
                last = e;
                log.warn("Mail send attempt {}/{} failed: {}", i + 1, props.getRetryAttempts(), e.getMessage());
                try { Thread.sleep(props.getRetryBackoffMs() * (i + 1)); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        throw new RuntimeException("Mail send failed after retries", last);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendAsync(MailRequest req) {
        try { send(req); } catch (Exception e) { log.error("Async mail failed: {}", e.getMessage(), e); }
        return CompletableFuture.completedFuture(null);
    }

    private void validate(MailRequest req) {
        List<String> all = new java.util.ArrayList<>();
        if (req.getTo() != null) all.addAll(req.getTo());
        if (req.getCc() != null) all.addAll(req.getCc());
        if (req.getBcc() != null) all.addAll(req.getBcc());
        if (all.isEmpty()) throw new IllegalArgumentException("Mail has no recipients");
        if (all.size() > props.getMaxRecipients()) {
            throw new IllegalArgumentException("Too many recipients: " + all.size());
        }
    }
}
