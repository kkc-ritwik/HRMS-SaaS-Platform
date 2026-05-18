package com.hrms.auth.service;

import com.hrms.auth.dto.OtpDto;
import com.hrms.mail.model.MailRequest;
import com.hrms.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_DIGITS = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int OTP_BOUND = 1_000_000;

    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;

    public String generateOtp(String email, OtpDto.OtpPurpose purpose) {
        String otp = String.format("%0" + OTP_DIGITS + "d",
                new SecureRandom().nextInt(OTP_BOUND));

        redisTemplate.opsForValue().set(redisKey(purpose, email), otp, OTP_TTL);
        sendOtpEmail(email, otp, purpose);
        return otp;
    }

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

    public void sendOtpEmail(String email, String otp, OtpDto.OtpPurpose purpose) {
        String subject = switch (purpose) {
            case LOGIN          -> "HRMS Login Verification Code";
            case SIGNUP         -> "HRMS Sign-up Verification Code";
            case RESET_PASSWORD -> "HRMS Password Reset Code";
        };
        String html = """
                <div style="font-family:Helvetica,Arial,sans-serif;max-width:520px;margin:0 auto;padding:24px;background:#f9fafb;border-radius:8px;">
                  <h2 style="color:#111827;">Your one-time code</h2>
                  <p style="color:#4b5563;">Use this code to complete your <strong>{{purpose}}</strong>. It is valid for <strong>5 minutes</strong> and can be used only once.</p>
                  <div style="font-size:28px;letter-spacing:8px;font-weight:700;background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:18px 24px;text-align:center;margin:24px 0;">{{otp}}</div>
                  <p style="color:#6b7280;font-size:12px;">If you didn't request this, you can ignore this email.</p>
                </div>
                """;
        mailService.sendAsync(MailRequest.builder()
                .to(email).subject(subject).html(html).category("auth-otp")
                .var("otp", otp).var("purpose", purpose.name().replace('_', ' ').toLowerCase())
                .build());
    }

    private String redisKey(OtpDto.OtpPurpose purpose, String email) {
        return "otp:" + purpose.name().toLowerCase() + ":" + email;
    }
}
