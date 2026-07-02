package com.hrms.recruitment;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.hrms.recruitment", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
@EnableJpaRepositories(basePackages = "com.hrms", considerNestedRepositories = true)
@EntityScan(basePackages = "com.hrms")
public class RecruitmentApp {
    public static void main(String[] args) {
        SpringApplication.run(RecruitmentApp.class, args);
    }
}
