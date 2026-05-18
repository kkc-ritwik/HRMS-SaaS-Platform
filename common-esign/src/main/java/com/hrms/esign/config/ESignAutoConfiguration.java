package com.hrms.esign.config;


import com.hrms.audit.annotation.Auditable;
import com.hrms.audit.listener.AuditEntityListener;
import jakarta.persistence.EntityListeners;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.hrms.esign")
@EntityScan(basePackages = "com.hrms.esign.model")
@EnableJpaRepositories(basePackages = "com.hrms.esign.service")
public class ESignAutoConfiguration {
}
