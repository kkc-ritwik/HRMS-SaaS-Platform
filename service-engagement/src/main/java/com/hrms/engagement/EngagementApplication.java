package com.hrms.engagement;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication @EnableDiscoveryClient @EnableScheduling
@EnableJpaRepositories(basePackages = "com.hrms", considerNestedRepositories = true)
@EntityScan(basePackages = "com.hrms")
@ComponentScan(basePackages = {"com.hrms.engagement", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class EngagementApplication {
    public static void main(String[] args) { SpringApplication.run(EngagementApplication.class, args); }
}
