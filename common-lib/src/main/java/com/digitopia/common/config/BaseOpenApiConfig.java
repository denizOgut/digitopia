package com.digitopia.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;


public abstract class BaseOpenApiConfig {

    protected OpenAPI createOpenAPI(String title, String version, String description) {
        return new OpenAPI()
            .info(new Info()
                .title(title)
                .version(version)
                .description(description)
                .contact(new Contact()
                    .name("Digitopia Team")
                    .email("info@digitopia.com")))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .in(SecurityScheme.In.HEADER)
                    .name("Authorization")))
            .addSecurityItem(new SecurityRequirement()
                .addList("bearer-jwt"));
    }
}
