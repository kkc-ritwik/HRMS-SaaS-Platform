package com.hrms.clients.config;

import feign.Logger;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@EnableFeignClients(basePackages = {"com.hrms"})
public class FeignAutoConfiguration {

    @Bean
    public Logger.Level feignLoggerLevel() { return Logger.Level.BASIC; }

    /** Forwards Authorization + tenant + correlation headers across service hops. */
    @Bean
    public RequestInterceptor headerForwardingInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            HttpServletRequest req = attrs.getRequest();
            forward(template, req, "Authorization");
            forward(template, req, "X-Tenant-Id");
            forward(template, req, "X-Request-Id");
            forward(template, req, "traceparent");
        };
    }

    private void forward(feign.RequestTemplate t, HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        if (v != null) t.header(name, v);
    }
}
