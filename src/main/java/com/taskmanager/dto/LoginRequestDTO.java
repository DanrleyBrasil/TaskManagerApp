package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de login.
 *
 * Aceita username OU email como identificador
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    /**
     * Username ou Email
     *
     * Flexibilidade: usuário pode fazer login com qualquer um
     * Exemplo: "joao" ou "joao@email.com"
     */
    @NotBlank(message = "Username ou email é obrigatório")
    private String usernameOrEmail;

    /**
     * Senha do usuário
     */
    @NotBlank(message = "Senha é obrigatória")
    private String password;
}