package com.hrms.compensation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.hrms.compensation", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class CompensationApp {
    public static void main(String[] args) {
        SpringApplication.run(CompensationApp.class, args);
    }
}
