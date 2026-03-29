package com.hrms.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.hrms.lms", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class LmsApp {
    public static void main(String[] args) {
        SpringApplication.run(LmsApp.class, args);
    }
}
