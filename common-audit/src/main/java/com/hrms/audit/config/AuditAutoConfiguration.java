package com.hrms.audit.config;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.hrms.audit.retention.RetentionPolicy;

@Configuration
@ComponentScan(basePackages = "com.hrms.audit")
@EntityScan(basePackages = "com.hrms.audit.entity")
@EnableJpaRepositories(basePackages = "com.hrms.audit.repository")
@EnableScheduling
@EnableConfigurationProperties(RetentionPolicy.class)
public class AuditAutoConfiguration {
}
