package com.hrms.auth.service;

import com.hrms.auth.dto.OtpDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_DIGITS = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int OTP_BOUND = 1_000_000;

    private final StringRedisTemplate redisTemplate;

    /**
     * Generates a 6-digit OTP, stores it in Redis with a 5-minute TTL,
     * and triggers the (currently logged) email delivery.
     */
    public String generateOtp(String email, OtpDto.OtpPurpose purpose) {
        String otp = String.format("%0" + OTP_DIGITS + "d",
                new SecureRandom().nextInt(OTP_BOUND));

        redisTemplate.opsForValue().set(redisKey(purpose, email), otp, OTP_TTL);

        sendOtpEmail(email, otp, purpose);
        return otp;
    }

    /**
     * Verifies the OTP against the Redis value.
     * Deletes the key on a successful match (one-time use).
     */
    public boolean verifyOtp(String email, String otp, OtpDto.OtpPurpose purpose) {
        String key = redisKey(purpose, email);
        String stored = redisTemplate.opsForValue().get(key);

        if (stored != null && stored.equals(otp)) {
            redisTemplate.delete(key);
            log.info("OTP verified for {} (purpose={})", email, purpose);
            return true;
        }

        log.warn("OTP verification failed for {} (purpose={})", email, purpose);
        return false;
    }

    /**
     * Sends an OTP email.
     * Currently logs the OTP; wire up a real JavaMailSender when SMTP is configured.
     */
    public void sendOtpEmail(String email, String otp, OtpDto.OtpPurpose purpose) {
        // TODO: replace with actual JavaMailSender call once SMTP is configured
        // Example HTML template usage:
        //   String html = buildHtmlTemplate(email, otp, purpose);
        //   MimeMessage msg = mailSender.createMimeMessage();
        //   new MimeMessageHelper(msg, true).setText(html, true);
        //   mailSender.send(msg);
        log.info("[OTP EMAIL] To={} | Purpose={} | Code={} | TTL=5min",
                email, purpose, otp);
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    /** Redis key: otp:{purpose_lowercase}:{email}  e.g. otp:login:user@example.com */
    private String redisKey(OtpDto.OtpPurpose purpose, String email) {
        return "otp:" + purpose.name().toLowerCase() + ":" + email;
    }
}
