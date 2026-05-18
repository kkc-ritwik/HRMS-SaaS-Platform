package com.hrms.mail.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
@EnableAsync
@ComponentScan(basePackages = "com.hrms.mail")
public class MailAutoConfiguration {
}
