package com.hrms.search.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hrms.search")
public class SearchProperties {
    private boolean enabled = true;
    private String uri = "http://localhost:9200";
    private String username;
    private String password;
    /** Index name prefix per environment, e.g. "prod-hrms" → prod-hrms-employees */
    private String indexPrefix = "hrms";
    private int connectTimeoutMs = 3000;
    private int socketTimeoutMs = 5000;
}
