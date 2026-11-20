package com.epdcl.webdisplay.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI webDisplayOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("WebDisplay Banking Application API")
                .description("REST API documentation. Existing session, role and CSRF protections remain active.")
                .version("1.0.0"));
    }
}
