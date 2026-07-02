package com.hrms.integrations;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication @EnableDiscoveryClient @EnableScheduling @EnableKafka
@EnableJpaRepositories(basePackages = "com.hrms", considerNestedRepositories = true)
@EntityScan(basePackages = "com.hrms")
@ComponentScan(basePackages = {"com.hrms.integrations", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class IntegrationsApplication {
    public static void main(String[] args) { SpringApplication.run(IntegrationsApplication.class, args); }
}
