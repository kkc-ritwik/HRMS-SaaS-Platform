package com.hrms.recruitment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.hrms.recruitment", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class RecruitmentApp {
    public static void main(String[] args) {
        SpringApplication.run(RecruitmentApp.class, args);
    }
}
