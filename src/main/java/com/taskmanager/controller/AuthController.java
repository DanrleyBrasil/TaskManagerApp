package com.taskmanager.controller;

import com.taskmanager.dto.JwtResponseDTO;
import com.taskmanager.dto.LoginRequestDTO;
import com.taskmanager.dto.RegisterRequestDTO;
import com.taskmanager.dto.UserResponseDTO;
import com.taskmanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável por autenticação e registro.
 *
 * @RestController:
 * - Combina @Controller + @ResponseBody
 * - Retorna JSON automaticamente (não views)
 *
 * @RequestMapping:
 * - Define URL base (/api/auth)
 * - Todos os métodos herdam esse prefixo
 *
 * @Tag (Swagger):
 * - Agrupa endpoints na documentação
 * - Aparece como seção "Autenticação" no Swagger UI
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para autenticação e registro de usuários")
public class AuthController {

    private final AuthService authService;

    /**
     * Registra um novo usuário.
     *
     * POST /api/auth/register
     *
     * @param dto - Dados do cadastro (username, email, password)
     * @return UserResponseDTO com dados do usuário criado
     *
     * @Valid:
     * - Valida anotações do DTO (@NotBlank, @Email, @Pattern)
     * - Se falhar → HTTP 400 com lista de erros
     *
     * @Operation (Swagger):
     * - Descreve o endpoint na documentação
     * - summary: título curto
     * - description: explicação detalhada
     *
     * @ApiResponses (Swagger):
     * - Documenta possíveis respostas HTTP
     * - 201: sucesso
     * - 400: validação falhou ou username/email já existe
     */
    @PostMapping("/register")
    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria uma nova conta de usuário no sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário registrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou username/email já existem"
            )
    })
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO dto
    ) {
        UserResponseDTO response = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Realiza login e retorna token JWT.
     *
     * POST /api/auth/login
     *
     * @param dto - Username/email e senha
     * @return JwtResponseDTO com token e dados do usuário
     *
     * Fluxo:
     * 1. AuthService valida credenciais
     * 2. Se válido → gera JWT
     * 3. Retorna token + dados do usuário
     * 4. Cliente guarda token e envia em próximas requisições
     *
     * Swagger:
     * - 200: login bem-sucedido
     * - 401: credenciais inválidas
     */
    @PostMapping("/login")
    @Operation(
            summary = "Fazer login",
            description = "Autentica usuário e retorna token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login bem-sucedido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas"
            )
    })
    public ResponseEntity<JwtResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto
    ) {
        JwtResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Retorna dados do usuário autenticado atual.
     *
     * GET /api/auth/me
     *
     * @return UserResponseDTO do usuário logado
     *
     * REQUER AUTENTICAÇÃO:
     * - Header: Authorization: Bearer {token}
     * - SecurityConfig libera apenas usuários autenticados
     *
     * Uso:
     * - Frontend busca dados do usuário logado
     * - Atualiza informações na UI
     *
     * Swagger:
     * - 200: sucesso
     * - 401: token inválido/ausente
     */
    @GetMapping("/me")
    @Operation(
            summary = "Obter usuário atual",
            description = "Retorna dados do usuário autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dados do usuário retornados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou ausente"
            )
    })
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        UserResponseDTO response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}