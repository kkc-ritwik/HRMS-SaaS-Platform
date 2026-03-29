package com.hrms.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.hrms.workflow", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class WorkflowApp {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowApp.class, args);
    }
}
