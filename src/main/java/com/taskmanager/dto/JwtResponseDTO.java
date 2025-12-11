package com.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO de resposta após login bem-sucedido.
 *
 * Retorna o token JWT e informações do usuário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {

    /**
     * Token JWT
     *
     * Cliente guarda esse token e envia em todas as requisições:
     * Authorization: Bearer {token}
     */
    private String token;

    /**
     * Tipo do token (sempre "Bearer")
     *
     * Padrão OAuth2/RFC 6750
     */
    private String type = "Bearer";

    /**
     * ID do usuário
     */
    private Long id;

    /**
     * Username do usuário
     */
    private String username;

    /**
     * Email do usuário
     */
    private String email;

    /**
     * Roles do usuário
     *
     * Set<String>: ["ROLE_USER"] ou ["ROLE_USER", "ROLE_ADMIN"]
     * Frontend usa isso para mostrar/ocultar funcionalidades
     */
    private Set<String> roles;

    /**
     * Construtor customizado sem o campo "type"
     * (type sempre será "Bearer")
     */
    public JwtResponseDTO(String token, Long id, String username, String email, Set<String> roles) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}