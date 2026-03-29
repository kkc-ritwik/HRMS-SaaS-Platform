package com.hrms.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Application-level configuration for service-auth.
 *
 * <p>Spring Boot's {@code MailSenderAutoConfiguration} auto-configures a
 * {@link JavaMailSender} from {@code spring.mail.*} properties when
 * {@code spring-boot-starter-mail} is on the classpath.  The bean below is a
 * safety-net: it activates only when no other {@link JavaMailSender} bean has
 * been registered (e.g. during local development without an SMTP server).</p>
 */
@Slf4j
@Configuration
public class AppConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    /**
     * Fallback {@link JavaMailSender} used when Spring Boot's auto-configuration
     * did not produce one (e.g. {@code spring.mail.host} is missing).
     *
     * <p>In production, remove the placeholder credentials in
     * {@code application.yml} and supply real values via environment variables
     * {@code MAIL_USERNAME} / {@code MAIL_PASSWORD}.</p>
     */
    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender fallbackMailSender() {
        log.warn("[AppConfig] No JavaMailSender bean found — creating fallback. "
                + "Configure spring.mail.* / MAIL_USERNAME / MAIL_PASSWORD for production.");

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailHost);
        sender.setPort(mailPort);
        sender.setUsername(mailUsername);
        sender.setPassword(mailPassword);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.debug", "false");

        return sender;
    }
}
