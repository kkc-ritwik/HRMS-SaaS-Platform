package com.hrms.events.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = "com.hrms.events")
@EntityScan(basePackages = "com.hrms.events.outbox")
@EnableJpaRepositories(basePackages = "com.hrms.events.outbox")
@EnableScheduling
public class EventsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper eventsObjectMapper() { return new ObjectMapper().findAndRegisterModules(); }
}
