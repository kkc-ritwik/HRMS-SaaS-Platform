package com.hrms.notification.dispatch.channels;

import com.hrms.notification.dispatch.DispatchRequest;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WhatsappChannel {

    @Value("${hrms.notification.twilio.whatsapp-from:}") private String whatsappFrom;

    public void send(DispatchRequest req) {
        if (whatsappFrom == null || whatsappFrom.isBlank() || req.getToPhones() == null) {
            log.warn("WhatsApp skipped (not configured / no recipients).");
            return;
        }
        String text = req.getBody() != null ? req.getBody() : req.getSubject();
        for (String to : req.getToPhones()) {
            try {
                String waTo = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;
                Message.creator(new PhoneNumber(waTo), new PhoneNumber(whatsappFrom), text).create();
            } catch (Exception e) {
                log.error("WhatsApp send to {} failed: {}", to, e.getMessage());
            }
        }
    }
}
