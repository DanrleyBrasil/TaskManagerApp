package com.taskmanager.config;

import com.taskmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração do Spring Security.
 *
 * @Configuration:
 * - Marca como classe de configuração
 * - Spring processa @Bean methods
 *
 * @EnableWebSecurity:
 * - Ativa Spring Security
 * - Habilita @PreAuthorize, @Secured, etc
 *
 * @EnableMethodSecurity:
 * - Permite usar @PreAuthorize nos métodos
 * - Exemplo: @PreAuthorize("hasRole('ADMIN')")
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserService userService;

    /**
     * Bean PasswordEncoder.
     *
     * BCryptPasswordEncoder:
     * - Algoritmo de hash BCrypt
     * - Automático: salt + hash
     * - Rounds: 10 (padrão) = 2^10 = 1024 iterações
     *
     * @Bean:
     * - Spring gerencia esse objeto
     * - Pode ser injetado em qualquer lugar
     * - Singleton (única instância)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean AuthenticationManager.
     *
     * AuthenticationManager:
     * - Responsável por autenticar usuários
     * - Usado no login (AuthService)
     *
     * AuthenticationConfiguration:
     * - Spring Boot auto-configura
     * - Já tem tudo configurado
     * - Só precisamos expor como Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configura a cadeia de filtros de segurança.
     *
     * SecurityFilterChain:
     * - Define regras de autorização
     * - Configura filtros
     * - Define políticas de sessão
     *
     * HttpSecurity:
     * - Builder fluente para configurar segurança
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /**
                 * CSRF (Cross-Site Request Forgery)
                 *
                 * Desabilitamos porque:
                 * - Usamos JWT (stateless)
                 * - CSRF só é problema com cookies/sessions (stateful)
                 * - API REST geralmente não usa CSRF
                 */
                .csrf(csrf -> csrf.disable())

                /**
                 * Regras de autorização
                 *
                 * ORDEM IMPORTA!
                 * - Spring Security processa na ordem declarada
                 * - Primeira regra que bater é aplicada
                 * - Sempre coloque regras mais específicas ANTES das genéricas
                 */
                .authorizeHttpRequests(auth -> auth
                        /**
                         * Endpoints PÚBLICOS (não requerem autenticação)
                         *
                         * SWAGGER/OPENAPI:
                         * - Rotas necessárias para documentação funcionar
                         * - JwtAuthenticationFilter pula essas rotas (shouldNotFilter)
                         * - Assim não tenta processar JWT para recursos estáticos
                         */
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        /**
                         * Autenticação (login, registro)
                         */
                        .requestMatchers("/api/auth/**").permitAll()

                        /**
                         * Endpoints ADMIN (apenas ROLE_ADMIN)
                         *
                         * hasRole("ADMIN"):
                         * - Exige role ADMIN
                         * - Spring Security adiciona "ROLE_" automaticamente
                         * - hasRole("ADMIN") → verifica "ROLE_ADMIN" no banco
                         */
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        /**
                         * Todos os outros endpoints
                         *
                         * authenticated():
                         * - Exige autenticação (token JWT válido)
                         * - Qualquer role serve (USER ou ADMIN)
                         */
                        .anyRequest().authenticated()
                )

                /**
                 * Política de sessão
                 *
                 * SessionCreationPolicy.STATELESS:
                 * - Spring Security NÃO cria sessão HTTP
                 * - NÃO usa cookies
                 * - Cada requisição é independente (precisa enviar JWT)
                 *
                 * Por que STATELESS?
                 * - JWT é stateless por natureza
                 * - Servidor não guarda estado
                 * - Escalabilidade horizontal
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /**
                 * Adiciona filtro JWT ANTES do filtro padrão
                 *
                 * addFilterBefore():
                 * - Nosso filtro executa PRIMEIRO
                 * - Valida JWT e seta autenticação
                 * - Depois executa filtros do Spring Security
                 *
                 * Ordem:
                 * 1. JwtAuthenticationFilter (nosso) ← Valida JWT
                 * 2. UsernamePasswordAuthenticationFilter (Spring)
                 * 3. FilterSecurityInterceptor (Spring) ← Valida permissões
                 * 4. Controller
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}