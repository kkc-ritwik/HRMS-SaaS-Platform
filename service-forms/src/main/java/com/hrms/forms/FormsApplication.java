package com.hrms.forms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication @EnableDiscoveryClient
public class FormsApplication {
    public static void main(String[] args) { SpringApplication.run(FormsApplication.class, args); }
}
