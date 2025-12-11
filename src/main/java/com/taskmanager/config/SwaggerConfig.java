package com.taskmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Swagger/OpenAPI 3.
 *
 * Gera documentação interativa da API.
 * Acesso: http://localhost:8080/swagger-ui.html
 *
 * Springdoc OpenAPI 3.0.0:
 * - Compatível com Spring Boot 4.x
 * - Substitui Springfox (descontinuado)
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configura OpenAPI (Swagger).
     *
     * OpenAPI:
     * - Especificação padrão para documentar APIs REST
     * - JSON/YAML descrevendo endpoints, schemas, autenticação
     * - Swagger UI renderiza visualmente
     */
    @Bean
    public OpenAPI customOpenAPI() {
        /**
         * Nome do scheme de segurança
         *
         * Será usado nos endpoints para indicar que precisam de JWT
         */
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                /**
                 * Informações da API
                 *
                 * Aparece no topo da documentação Swagger
                 */
                .info(new Info()
                        .title("Task Manager API")
                        .description("API RESTful para gerenciamento de tarefas com autenticação JWT")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Danrley")
                                .url("https://github.com/DanrleyBrasil")
                                .email("contato@danrley.dev")
                        )
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")
                        )
                )

                /**
                 * Configuração de segurança JWT
                 *
                 * addSecurityItem():
                 * - Aplica segurança globalmente (todos os endpoints)
                 * - Usuário pode adicionar JWT no topo da página Swagger
                 *
                 * SecurityRequirement:
                 * - Nome do scheme (definido abaixo em securitySchemes)
                 */
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName)
                )

                /**
                 * Define o scheme de segurança JWT
                 *
                 * Components:
                 * - Componentes reutilizáveis (schemas, security schemes, etc)
                 *
                 * SecurityScheme:
                 * - type: HTTP (não OAuth, não API Key)
                 * - scheme: bearer (padrão OAuth2/RFC 6750)
                 * - bearerFormat: JWT (formato do token)
                 *
                 * Swagger UI:
                 * - Mostra botão "Authorize" no topo
                 * - Usuário cola o JWT
                 * - Todas as requisições incluem: Authorization: Bearer {JWT}
                 */
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT (apenas o token, sem 'Bearer ')")
                        )
                );
    }
}