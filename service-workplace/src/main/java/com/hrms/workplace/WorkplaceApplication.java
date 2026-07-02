package com.hrms.workplace;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication @EnableDiscoveryClient
@EnableJpaRepositories(basePackages = "com.hrms", considerNestedRepositories = true)
@EntityScan(basePackages = "com.hrms")
@ComponentScan(basePackages = {"com.hrms.workplace", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class WorkplaceApplication {
    public static void main(String[] args) { SpringApplication.run(WorkplaceApplication.class, args); }
}
