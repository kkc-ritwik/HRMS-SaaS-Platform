package com.hrms.mail.config;

import com.hrms.mail.service.InlineTemplateRenderer;
import com.hrms.mail.service.TemplateRenderer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
@EnableAsync
@ComponentScan(basePackages = "com.hrms.mail")
public class MailAutoConfiguration {

    /** Default mustache renderer; overridden by any service that supplies its own TemplateRenderer. */
    @Bean
    @ConditionalOnMissingBean(TemplateRenderer.class)
    public TemplateRenderer inlineTemplateRenderer() {
        return new InlineTemplateRenderer();
    }
}
