package com.taskmanager.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de cadastro de usuário.
 *
 * @Data (Lombok):
 * - Gera @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
 * - Reduz boilerplate
 *
 * Validações (Bean Validation):
 * - @NotBlank: não pode ser null, vazio ou só espaços
 * - @Size: tamanho mínimo/máximo
 * - @Email: valida formato de email
 * - @Pattern: valida regex (senha forte)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    /**
     * Nome de usuário (login)
     *
     * @NotBlank:
     * - null → erro
     * - "" → erro
     * - "   " → erro (só espaços)
     *
     * @Size:
     * - min = 3: mínimo 3 caracteres
     * - max = 50: máximo 50 (bate com banco)
     *
     * message:
     * - Mensagem customizada de erro
     * - Retornada na resposta HTTP 400
     */
    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;

    /**
     * Email do usuário
     *
     * @Email:
     * - Valida formato: usuario@dominio.com
     * - Aceita: teste@email.com.br
     * - Rejeita: teste@, @teste.com, teste.com
     */
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;

    /**
     * Senha do usuário
     *
     * @Pattern:
     * - Regex para senha forte
     * - Requisitos:
     *   (?=.*[a-z]) → Pelo menos 1 letra minúscula
     *   (?=.*[A-Z]) → Pelo menos 1 letra maiúscula
     *   (?=.*\d)    → Pelo menos 1 número
     *   (?=.*[@$!%*?&]) → Pelo menos 1 caractere especial
     *   [A-Za-z\d@$!%*?&]{8,} → Mínimo 8 caracteres
     *
     * Exemplos válidos: "Senha123!", "Teste@2024"
     * Exemplos inválidos: "senha123", "SENHA123", "Senha"
     */
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 255, message = "Senha deve ter entre 8 e 255 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Senha deve conter: letra maiúscula, minúscula, número e caractere especial"
    )
    private String password;
}