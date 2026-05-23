package com.hrms.security.propagation;

import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestClient;

import java.io.IOException;

/**
 * Inter-service header propagation. Every outbound RestTemplate / RestClient call
 * automatically re-attaches:
 *   · X-Tenant-Id          — current tenant
 *   · X-User-Id, X-User-Email — current principal
 *   · X-Request-Id         — trace correlation
 *   · Authorization        — pass-through bearer token for federated calls
 *
 * Without this, the downstream service has no idea who called it, breaks multi-tenancy
 * and audit logs, and shows up as anonymous in tracing.
 */
@Configuration
@ConditionalOnClass(RestClient.class)
public class TenantPropagationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String tenant = TenantContext.get();
        if (tenant != null && !request.getHeaders().containsKey("X-Tenant-Id")) {
            request.getHeaders().add("X-Tenant-Id", tenant);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal up) {
            if (!request.getHeaders().containsKey("X-User-Id"))
                request.getHeaders().add("X-User-Id", String.valueOf(up.getId()));
            if (up.getEmail() != null && !request.getHeaders().containsKey("X-User-Email"))
                request.getHeaders().add("X-User-Email", up.getEmail());
        }
        String rid = MDC.get("requestId");
        if (rid != null && !request.getHeaders().containsKey("X-Request-Id")) {
            request.getHeaders().add("X-Request-Id", rid);
        }
        // Pass-through bearer for federated calls
        HttpServletRequest in = currentRequest();
        if (in != null && !request.getHeaders().containsKey("Authorization")) {
            String authz = in.getHeader("Authorization");
            if (authz != null && authz.startsWith("Bearer ")) {
                request.getHeaders().add("Authorization", authz);
            }
        }
        return execution.execute(request, body);
    }

    private static HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
    }

    @Bean
    public RestClient.Builder propagatingRestClientBuilder() {
        return RestClient.builder().requestInterceptor(this);
    }
}
