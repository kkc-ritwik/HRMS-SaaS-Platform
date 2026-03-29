package com.hrms.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.hrms.social", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class SocialApp {
    public static void main(String[] args) {
        SpringApplication.run(SocialApp.class, args);
    }
}
