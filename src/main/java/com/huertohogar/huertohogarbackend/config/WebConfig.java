package com.huertohogar.huertohogarbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // Configuración para permitir peticiones desde el origen del frontend
        registry.addMapping("/**") // Aplica la configuración a TODOS los endpoints ("/**")

                // ORÍGENES PERMITIDOS: Aquí debes listar las URLs de tu frontend
                .allowedOrigins(
                        "http://localhost:3000", // Ejemplo: React, Vite
                        "http://localhost:4200", // Ejemplo: Angular
                        "http://localhost:8081"  // Ejemplo: Otro puerto de desarrollo
                )

                // MÉTODOS HTTP PERMITIDOS: Permite todos los verbos REST
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                // CABECERAS PERMITIDAS: Permite todas las cabeceras (incluyendo Content-Type y Authorization)
                .allowedHeaders("*")

                // PERMITE CREDENCIALES: Fundamental para la seguridad JWT
                // Permite que las cabeceras de autorización (como el token JWT) sean enviadas.
                .allowCredentials(true)

                // Tiempo que el navegador puede guardar en caché los resultados de la pre-verificación CORS (en segundos)
                .maxAge(3600);
    }
}