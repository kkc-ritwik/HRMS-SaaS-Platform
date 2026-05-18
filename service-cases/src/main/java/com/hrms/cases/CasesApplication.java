package com.hrms.cases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication @EnableDiscoveryClient
public class CasesApplication {
    public static void main(String[] args) { SpringApplication.run(CasesApplication.class, args); }
}
