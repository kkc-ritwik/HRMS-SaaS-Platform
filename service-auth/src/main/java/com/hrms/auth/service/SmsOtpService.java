package com.hrms.auth.service;

import com.hrms.auth.dto.OtpDto;
import com.hrms.clients.notification.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * SMS-based OTP alternative for password reset / login on devices without email. Sends via
 * service-notification's SMS channel (Twilio). Same 5-min Redis TTL as email OTP, but a
 * separate key namespace so SMS + email OTPs don't collide.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsOtpService {

    private static final int OTP_DIGITS = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int OTP_BOUND = 1_000_000;

    private final StringRedisTemplate redis;
    private final ObjectProvider<NotificationClient> notificationClientProvider;

    public String generateAndSend(String tenantId, String phoneE164, OtpDto.OtpPurpose purpose) {
        String otp = String.format("%0" + OTP_DIGITS + "d", new SecureRandom().nextInt(OTP_BOUND));
        redis.opsForValue().set(redisKey(purpose, phoneE164), otp, OTP_TTL);

        NotificationClient nc = notificationClientProvider.getIfAvailable();
        if (nc == null) {
            log.warn("NotificationClient unavailable — SMS OTP {} (purpose={}) for phone {} not sent",
                    otp, purpose, phoneE164);
            return otp;
        }
        String body = "HRMS: Your " + purpose.name().toLowerCase().replace('_',' ')
                + " code is " + otp + ". Valid for 5 minutes. Do not share.";
        nc.dispatch(new NotificationClient.NotificationDispatchRequest(
                tenantId, List.of(),                  // recipientUserIds (n/a — direct phone)
                null,                                  // templateCode
                "SMS",                                 // channel
                "HRMS Verification Code",              // subject
                Map.of("phones", List.of(phoneE164), "body", body),
                "auth-otp", null, "URGENT"));
        return otp;
    }

    public boolean verify(String phoneE164, String otp, OtpDto.OtpPurpose purpose) {
        String key = redisKey(purpose, phoneE164);
        String stored = redis.opsForValue().get(key);
        if (stored != null && stored.equals(otp)) {
            redis.delete(key);
            return true;
        }
        return false;
    }

    private String redisKey(OtpDto.OtpPurpose purpose, String phone) {
        return "otp:sms:" + purpose.name().toLowerCase() + ":" + phone;
    }
}
