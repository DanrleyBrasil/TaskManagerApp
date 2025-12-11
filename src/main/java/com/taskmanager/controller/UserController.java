package com.taskmanager.controller;

import com.taskmanager.dto.UserResponseDTO;
import com.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gerenciamento de perfil do usuário.
 *
 * Endpoints relacionados ao próprio usuário autenticado.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de perfil do usuário")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * Busca perfil do usuário autenticado.
     *
     * GET /api/users/me
     *
     * @param authentication - Usuário autenticado
     * @return UserResponseDTO
     *
     * NOTA: Endpoint duplicado com /api/auth/me
     * - Você pode escolher manter apenas um
     * - Ou manter ambos para flexibilidade
     */
    @GetMapping("/me")
    @Operation(
            summary = "Obter perfil do usuário",
            description = "Retorna dados do perfil do usuário autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            )
    })
    public ResponseEntity<UserResponseDTO> getProfile(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        UserResponseDTO user = new UserResponseDTO(userService.findByUsername(username));
        return ResponseEntity.ok(user);
    }

    /**
     * Busca usuário por username (público).
     *
     * GET /api/users/{username}
     *
     * @param username - Username do usuário
     * @return UserResponseDTO
     *
     * NOTA: Este endpoint permite ver dados de OUTROS usuários
     * - Você pode restringir ou remover se necessário
     * - Útil para funcionalidades sociais (ex: compartilhar tarefa com outro usuário)
     */
    @GetMapping("/{username}")
    @Operation(
            summary = "Buscar usuário por username",
            description = "Retorna dados públicos de um usuário"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário não encontrado"
            )
    })
    public ResponseEntity<UserResponseDTO> getUserByUsername(
            @Parameter(description = "Username do usuário", example = "joao")
            @PathVariable String username
    ) {
        UserResponseDTO user = new UserResponseDTO(userService.findByUsername(username));
        return ResponseEntity.ok(user);
    }
}