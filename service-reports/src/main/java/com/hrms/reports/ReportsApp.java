package com.hrms.reports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableScheduling
@ComponentScan(basePackages = {"com.hrms.reports", "com.hrms.common", "com.hrms.security", "com.hrms.tenant"})
public class ReportsApp {
    public static void main(String[] args) {
        SpringApplication.run(ReportsApp.class, args);
    }
}
