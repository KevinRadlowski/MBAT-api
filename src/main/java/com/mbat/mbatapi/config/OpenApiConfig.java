package com.mbat.mbatapi.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI (Swagger) pour l'API.
 * Cette classe définit les informations de base affichées dans l'interface Swagger.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Crée et configure l'objet OpenAPI avec des informations générales sur l'API.
     *
     * @return Un objet OpenAPI configuré avec les informations sur l'API.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestion des Comptes")
                        .version("1.0")
                        .description("Documentation de l'API de gestion des comptes bancaires")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentation Complète")
                        .url("https://github.com/mbat-mbatapi"));
    }
}
