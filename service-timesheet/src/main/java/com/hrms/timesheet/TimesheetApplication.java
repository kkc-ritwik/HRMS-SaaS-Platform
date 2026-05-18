package com.hrms.timesheet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication @EnableDiscoveryClient
public class TimesheetApplication {
    public static void main(String[] args) { SpringApplication.run(TimesheetApplication.class, args); }
}
