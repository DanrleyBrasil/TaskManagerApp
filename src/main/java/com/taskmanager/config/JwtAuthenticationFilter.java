package com.taskmanager.config;

import com.taskmanager.service.JwtService;
import com.taskmanager.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro JWT que valida o token em cada requisição.
 *
 * OncePerRequestFilter:
 * - Garante que o filtro é executado apenas 1 vez por requisição
 * - Spring pode chamar filtros múltiplas vezes em alguns cenários (forward, include)
 *
 * Fluxo:
 * 1. Requisição chega → Filtro intercepta
 * 2. Verifica se é rota excluída (Swagger, etc)
 * 3. Extrai token do header Authorization
 * 4. Valida token (assinatura, expiração)
 * 5. Se válido → carrega usuário e seta autenticação
 * 6. Passa para próximo filtro/controller
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    /**
     * Lista de paths que devem ser ignorados pelo filtro JWT.
     *
     * CRÍTICO: Filtro precisa pular Swagger e recursos estáticos!
     * - Se não pular, tenta processar JWT para rotas públicas
     * - Causa erros 500 quando Swagger tenta carregar
     *
     * Paths excluídos:
     * - /swagger-ui → Interface do Swagger
     * - /v3/api-docs → Documentação OpenAPI JSON
     * - /swagger-ui.html → Página inicial do Swagger
     * - /api-docs → Configuração do Swagger (swagger-config)
     * - /swagger-resources → Recursos do Swagger
     * - /webjars → Bibliotecas JS/CSS
     */
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-ui.html",
            "/api-docs",
            "/swagger-resources",
            "/webjars"
    );

    /**
     * Verifica se a requisição deve ser ignorada pelo filtro JWT.
     *
     * Sobrescreve método do OncePerRequestFilter para definir
     * quais requisições devem pular o filtro completamente.
     *
     * @param request - HttpServletRequest
     * @return true se deve pular o filtro, false se deve processar
     * @throws ServletException se houver erro na verificação
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Verifica se o path começa com algum dos paths excluídos
        return EXCLUDED_PATHS.stream()
                .anyMatch(path::startsWith);
    }

    /**
     * Método principal do filtro.
     *
     * Executado em TODA requisição (exceto as definidas em shouldNotFilter).
     *
     * IMPORTANTE:
     * - shouldNotFilter() é chamado ANTES deste método
     * - Se retornar true, este método NÃO é executado
     * - Isso evita processar JWT para rotas públicas (Swagger, etc)
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extrai token do header Authorization
            String jwt = getJwtFromRequest(request);

            /**
             * 2. Valida token
             *
             * Condições para processar:
             * - Token existe (não é null)
             * - Token tem conteúdo (não é vazio)
             * - Token é válido (assinatura e expiração)
             */
            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {

                // 3. Extrai username do token
                String username = jwtService.getUsernameFromToken(jwt);

                /**
                 * 4. Carrega dados do usuário
                 *
                 * UserDetailsService.loadUserByUsername():
                 * - Busca usuário no banco
                 * - Retorna UserDetails com username, password, roles
                 */
                UserDetails userDetails = userService.loadUserByUsername(username);

                /**
                 * 5. Cria objeto de autenticação
                 *
                 * UsernamePasswordAuthenticationToken:
                 * - Representa um usuário autenticado
                 * - (principal, credentials, authorities)
                 * - principal: UserDetails (dados do usuário)
                 * - credentials: null (não precisamos da senha depois de autenticado)
                 * - authorities: roles do usuário
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                /**
                 * 6. Adiciona detalhes da requisição
                 *
                 * WebAuthenticationDetailsSource:
                 * - Adiciona IP, session ID, etc
                 * - Útil para auditoria
                 */
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                /**
                 * 7. Seta autenticação no contexto do Spring Security
                 *
                 * IMPORTANTE:
                 * - A partir daqui, o usuário está autenticado
                 * - Controllers podem acessar via @AuthenticationPrincipal
                 * - Spring Security valida permissões (@PreAuthorize)
                 */
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception ex) {
            /**
             * Se der erro, apenas loga
             *
             * NÃO retorna erro aqui!
             * - Deixa Spring Security tratar (retorna 401)
             * - Se bloquear aqui, quebra endpoints públicos
             */
            System.err.println("Não foi possível setar autenticação do usuário: " + ex.getMessage());
        }

        /**
         * 8. Passa para próximo filtro
         *
         * CRÍTICO: SEMPRE chamar filterChain.doFilter()!
         * - Se não chamar, requisição para aqui (timeout)
         * - filterChain continua a cadeia de filtros até o Controller
         */
        filterChain.doFilter(request, response);
    }

    /**
     * Extrai JWT do header Authorization.
     *
     * Header esperado:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     *
     * Formato:
     * - "Bearer " (com espaço) + token
     * - Remove o prefixo "Bearer " e retorna só o token
     *
     * @param request - HttpServletRequest
     * @return Token JWT (sem "Bearer ") ou null se não existir
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Pega o header Authorization
        String bearerToken = request.getHeader("Authorization");

        /**
         * Valida formato:
         * - StringUtils.hasText() → não é null/vazio
         * - startsWith("Bearer ") → tem prefixo correto
         */
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Remove "Bearer " (7 caracteres) e retorna token
            return bearerToken.substring(7);
        }

        return null;
    }
}