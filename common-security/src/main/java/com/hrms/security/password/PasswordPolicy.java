package com.hrms.security.password;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "hrms.security.password")
public class PasswordPolicy {
    private int minLength = 10;
    private int maxLength = 128;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireDigit = true;
    private boolean requireSpecial = true;
    private int historyLookback = 5;
    private int maxAgeDays = 90;
    private List<String> blockedPatterns = List.of("password", "qwerty", "12345", "hrms");

    public PasswordValidation validate(String pwd) {
        if (pwd == null || pwd.length() < minLength) return PasswordValidation.fail("Min length " + minLength);
        if (pwd.length() > maxLength) return PasswordValidation.fail("Max length " + maxLength);
        if (requireUppercase && !pwd.chars().anyMatch(Character::isUpperCase))
            return PasswordValidation.fail("Must contain uppercase");
        if (requireLowercase && !pwd.chars().anyMatch(Character::isLowerCase))
            return PasswordValidation.fail("Must contain lowercase");
        if (requireDigit && !pwd.chars().anyMatch(Character::isDigit))
            return PasswordValidation.fail("Must contain digit");
        if (requireSpecial && pwd.chars().allMatch(Character::isLetterOrDigit))
            return PasswordValidation.fail("Must contain special character");
        String lower = pwd.toLowerCase();
        for (String b : blockedPatterns) {
            if (lower.contains(b)) return PasswordValidation.fail("Contains weak pattern: " + b);
        }
        return PasswordValidation.ok();
    }

    public record PasswordValidation(boolean valid, String reason) {
        public static PasswordValidation ok() { return new PasswordValidation(true, null); }
        public static PasswordValidation fail(String r) { return new PasswordValidation(false, r); }
    }
}
