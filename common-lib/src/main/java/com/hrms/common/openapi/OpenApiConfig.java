package com.hrms.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Adds a JWT bearer security scheme + an API-key scheme to every service's OpenAPI doc.
 * Swagger UI then renders a "Authorize" button so devs can paste a token and try APIs
 * inside the browser.
 */
@Configuration
@ConditionalOnClass(OpenAPI.class)
public class OpenApiConfig {

    @Value("${spring.application.name:hrms}")
    private String appName;

    @Value("${hrms.openapi.contact-email:platform@hrms.local}")
    private String contactEmail;

    @Bean
    public OpenAPI hrmsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HRMS – " + appName)
                        .version("1.0.0")
                        .description("HRMS SaaS Platform — multi-tenant Zoho People parity. "
                                + "All endpoints require a JWT bearer token unless explicitly marked public. "
                                + "Send `X-Tenant-Id` and `Idempotency-Key` headers per platform conventions.")
                        .contact(new Contact().name("HRMS Platform").email(contactEmail))
                        .license(new License().name("Proprietary")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local gateway"),
                        new Server().url("https://api.hrms.example.com").description("Production")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT access token returned by POST /api/auth/login"))
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Server-to-server API key (machine accounts only)"))
                        .addSecuritySchemes("tenantHeader", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Tenant-Id")
                                .description("Tenant ID. Required for all multi-tenant endpoints.")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth").addList("tenantHeader"));
    }
}
