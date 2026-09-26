package com.example.sandboxspei.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de metadatos de la documentación OpenAPI/Swagger.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApiInfo() {
        return new OpenAPI().info(new Info()
                .title("PRAXTHON AMATEUR 2026 - Sandbox SPEI")
                .description("API sandbox de simulación de operaciones de pago SPEI")
                .version("1.0.0")
                .contact(new Contact().name("Equipo PRAXTHON")));
    }
}
