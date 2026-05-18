package com.hrms.integrations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication @EnableDiscoveryClient @EnableScheduling @EnableKafka
public class IntegrationsApplication {
    public static void main(String[] args) { SpringApplication.run(IntegrationsApplication.class, args); }
}
