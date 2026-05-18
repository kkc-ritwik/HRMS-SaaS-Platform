package com.hrms.notification.dispatch.channels;

import com.hrms.notification.dispatch.DispatchRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsChannel {

    @Value("${hrms.notification.twilio.account-sid:}")  private String accountSid;
    @Value("${hrms.notification.twilio.auth-token:}")   private String authToken;
    @Value("${hrms.notification.twilio.from-number:}") private String fromNumber;

    private boolean enabled = false;

    @PostConstruct
    void init() {
        if (accountSid != null && !accountSid.isBlank() && !accountSid.startsWith("REPLACE")) {
            try {
                Twilio.init(accountSid, authToken);
                enabled = true;
                log.info("SMS channel (Twilio) initialized from {}", fromNumber);
            } catch (Exception e) {
                log.warn("Twilio init failed — SMS channel disabled: {}", e.getMessage());
            }
        } else {
            log.warn("Twilio credentials not configured — SMS channel disabled.");
        }
    }

    public void send(DispatchRequest req) {
        if (!enabled) { log.warn("SMS skipped (not configured) — to={}", req.getToPhones()); return; }
        if (req.getToPhones() == null) return;
        String text = req.getBody() != null ? req.getBody() : req.getSubject();
        for (String to : req.getToPhones()) {
            try {
                Message.creator(new PhoneNumber(to), new PhoneNumber(fromNumber), text).create();
                log.info("SMS sent to {}", to);
            } catch (Exception e) {
                log.error("SMS send to {} failed: {}", to, e.getMessage());
            }
        }
    }
}
