package com.huertohogar.huertohogarbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "HuertoHogar Backend API",
                version = "1.0",
                description = "API REST para la gestión de usuarios, roles y productos de HuertoHogar."
        )
)
@SecurityScheme(
        name = "bearerAuth", // Nombre que se usa en la interfaz de Swagger
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer" // Prefijo "Bearer "
)
public class OpenApiConfig {
    // La configuración principal se define en las anotaciones.
}