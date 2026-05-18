package com.hrms.helpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan(basePackages = {"com.hrms.helpdesk", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class HelpdeskApp {
    public static void main(String[] args) {
        SpringApplication.run(HelpdeskApp.class, args);
    }
}
