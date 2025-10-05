package com.digitopia.invitation.infrastructure.config;

import com.digitopia.common.config.BaseOpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig extends BaseOpenApiConfig {

    @Bean
    public OpenAPI invitationServiceOpenAPI() {
        return createOpenAPI(
            "Invitation Service API",
            "1.0.0",
            "Organization invitation management"
        );
    }
}
