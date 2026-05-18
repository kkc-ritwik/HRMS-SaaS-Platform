package com.hrms.security.lockout;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "hrms.security.lockout")
public class AccountLockoutPolicy {
    private boolean enabled = true;
    private int maxFailedAttempts = 5;
    private int lockoutMinutes = 15;
    private int resetCounterAfterMinutes = 30;
    private int captchaAfterFailures = 3;
}
