package com.hrms.offboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.hrms.offboarding", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class OffboardingApp {
    public static void main(String[] args) {
        SpringApplication.run(OffboardingApp.class, args);
    }
}
