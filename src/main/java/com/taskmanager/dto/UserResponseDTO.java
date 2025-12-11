package com.taskmanager.dto;

import com.taskmanager.model.RoleName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO de resposta com dados do usuário.
 *
 * ATENÇÃO: NÃO contém senha!
 * Nunca retorne senhas em APIs, mesmo criptografadas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private Set<RoleName> roles; // Apenas os nomes dos roles
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Construtor de conveniência para converter User → UserResponseDTO
     *
     * Uso no Service:
     * User user = userRepository.findById(id);
     * return new UserResponseDTO(user);
     */
    public UserResponseDTO(com.taskmanager.model.User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(java.util.stream.Collectors.toSet());
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }
}