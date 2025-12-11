package com.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing).
 *
 * CORS:
 * - Política de segurança do navegador
 * - Impede que site A acesse API do site B (sem permissão)
 *
 * Cenário:
 * - Frontend: http://localhost:3000 (React, Angular, Vue)
 * - Backend: http://localhost:8080 (Spring Boot)
 * - Sem CORS → Navegador bloqueia requisições!
 * - Com CORS → Backend permite origens específicas
 *
 * IMPORTANTE:
 * - Em produção, especifique origins exatas (não use "*")
 * - "*" permite QUALQUER site acessar sua API (perigoso!)
 */
@Configuration
public class CorsConfig {

    /**
     * Configura CORS globalmente.
     *
     * CorsConfigurationSource:
     * - Spring Security usa isso para validar origens
     * - Aplica regras ANTES dos filtros de segurança
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        /**
         * Origins permitidas
         *
         * setAllowedOrigins():
         * - Lista de URLs que podem acessar a API
         *
         * DESENVOLVIMENTO:
         * - http://localhost:3000 (React)
         * - http://localhost:4200 (Angular)
         * - http://localhost:5173 (Vite)
         *
         * PRODUÇÃO:
         * - https://meusite.com
         * - https://app.meusite.com
         *
         * ⚠️ NUNCA use "*" em produção!
         */
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",   // React
                "http://localhost:4200",   // Angular
                "http://localhost:5173",   // Vite
                "http://localhost:8081"    // Outro frontend
                // Em produção: adicione seu domínio real
        ));

        /**
         * Métodos HTTP permitidos
         *
         * GET, POST, PUT, DELETE, PATCH, OPTIONS
         *
         * OPTIONS:
         * - Navegador envia OPTIONS (preflight) antes de requisição real
         * - Valida se servidor permite a requisição
         * - CORS precisa aceitar OPTIONS!
         */
        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        /**
         * Headers permitidos
         *
         * Authorization:
         * - CRÍTICO! Precisa permitir para JWT funcionar
         * - Sem isso, navegador bloqueia header Authorization
         *
         * Content-Type:
         * - Necessário para enviar JSON
         *
         * Accept:
         * - Cliente especifica formato desejado (JSON, XML, etc)
         *
         * "*" aqui é seguro (não expõe dados sensíveis)
         */
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        /**
         * Headers expostos (que navegador pode ler)
         *
         * Por padrão, navegador só lê headers simples:
         * - Content-Type, Content-Length, etc
         *
         * Se servidor retorna headers customizados:
         * - Authorization (novo JWT após refresh)
         * - X-Total-Count (paginação)
         *
         * Precisa expor explicitamente!
         */
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization"
        ));

        /**
         * Permite credenciais
         *
         * setAllowCredentials(true):
         * - Permite enviar cookies, Authorization header, etc
         * - NECESSÁRIO para JWT funcionar!
         *
         * ⚠️ Se true, NÃO pode usar "*" em allowedOrigins!
         * - Precisa especificar origins exatas
         */
        configuration.setAllowCredentials(true);

        /**
         * Max age (cache de preflight)
         *
         * 3600 segundos = 1 hora
         * - Navegador cacheia resposta do OPTIONS
         * - Não precisa enviar OPTIONS a cada requisição
         * - Performance!
         */
        configuration.setMaxAge(3600L);

        /**
         * Registra configuração para todas as URLs
         *
         * UrlBasedCorsConfigurationSource:
         * - Mapeia padrões de URL → configuração CORS
         *
         * "/**":
         * - Aplica para TODOS os endpoints
         * - /api/auth/**, /api/tasks/**, /swagger-ui/**, etc
         */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}