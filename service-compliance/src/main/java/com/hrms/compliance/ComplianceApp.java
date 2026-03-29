package com.hrms.compliance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.hrms.compliance", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class ComplianceApp {
    public static void main(String[] args) {
        SpringApplication.run(ComplianceApp.class, args);
    }
}
