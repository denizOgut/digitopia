package com.digitopia.user.infrastructure.config;

import com.digitopia.common.config.BaseOpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig extends BaseOpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return createOpenAPI(
            "User Service API",
            "1.0.0",
            "User management and authentication endpoints"
        );
    }
}
