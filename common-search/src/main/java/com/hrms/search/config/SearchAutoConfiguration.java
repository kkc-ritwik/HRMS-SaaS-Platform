package com.hrms.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(SearchProperties.class)
@ComponentScan(basePackages = "com.hrms.search")
public class SearchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchClient elasticsearchClient(SearchProperties p) {
        URI u = URI.create(p.getUri());
        var builder = RestClient.builder(new HttpHost(u.getHost(),
                u.getPort() < 0 ? 9200 : u.getPort(), u.getScheme()));

        if (p.getUsername() != null && !p.getUsername().isBlank()) {
            BasicCredentialsProvider cp = new BasicCredentialsProvider();
            cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(p.getUsername(), p.getPassword()));
            builder.setHttpClientConfigCallback(cb -> cb.setDefaultCredentialsProvider(cp));
        }
        builder.setRequestConfigCallback(rc -> rc
                .setConnectTimeout(p.getConnectTimeoutMs())
                .setSocketTimeout(p.getSocketTimeoutMs()));
        RestClient rest = builder.build();
        return new ElasticsearchClient(new RestClientTransport(rest, new JacksonJsonpMapper()));
    }
}
