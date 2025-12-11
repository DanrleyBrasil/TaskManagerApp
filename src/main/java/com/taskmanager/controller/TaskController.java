package com.taskmanager.controller;

import com.taskmanager.dto.TaskRequestDTO;
import com.taskmanager.dto.TaskResponseDTO;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsável pelo CRUD de tarefas.
 *
 * TODOS OS ENDPOINTS REQUEREM AUTENTICAÇÃO:
 * - Header: Authorization: Bearer {token}
 * - SecurityConfig: .anyRequest().authenticated()
 *
 * @SecurityRequirement (Swagger):
 * - Indica que endpoints precisam de JWT
 * - Mostra cadeado nos endpoints no Swagger UI
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tarefas", description = "Endpoints para gerenciamento de tarefas")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService; //

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    /**
     * Extrai ID do usuário autenticado.
     *
     * CORRETO:
     * - TaskController não acessa Repository diretamente
     * - TaskController chama Service (camada apropriada)
     * - UserService encapsula lógica de usuário
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // ✅ CORRETO: Chama UserService (não Repository)
        return userService.getIdByUsername(username);
    }

    /**
     * Cria uma nova tarefa.
     *
     * POST /api/tasks
     *
     * @param dto - Dados da tarefa (title, description, status)
     * @param authentication - Usuário autenticado (injetado automaticamente)
     * @return TaskResponseDTO da tarefa criada
     *
     * Fluxo:
     * 1. Extrai userId do token JWT
     * 2. TaskService cria tarefa associada ao usuário
     * 3. Retorna tarefa criada
     *
     * Swagger:
     * - 201: tarefa criada
     * - 400: dados inválidos
     * - 401: token inválido
     */
    @PostMapping
    @Operation(
            summary = "Criar nova tarefa",
            description = "Cria uma tarefa para o usuário autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Tarefa criada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            )
    })
    public ResponseEntity<TaskResponseDTO> createTask(
            @Valid @RequestBody TaskRequestDTO dto,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        TaskResponseDTO response = taskService.createTask(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todas as tarefas do usuário autenticado (sem paginação).
     *
     * GET /api/tasks
     *
     * @param authentication - Usuário autenticado
     * @return Lista de TaskResponseDTO
     *
     * NOTA: Use este endpoint com cuidado!
     * - Se usuário tem 10.000 tarefas, retorna TODAS
     * - Prefira o endpoint com paginação (/api/tasks/paginated)
     */
    @GetMapping
    @Operation(
            summary = "Listar todas as tarefas",
            description = "Retorna todas as tarefas do usuário autenticado (sem paginação)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de tarefas retornada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            )
    })
    public ResponseEntity<List<TaskResponseDTO>> getAllTasks(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<TaskResponseDTO> tasks = taskService.findAllByUserId(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Lista tarefas do usuário com paginação.
     *
     * GET /api/tasks/paginated?page=0&size=10&sort=createdAt,desc
     *
     * @param pageable - Configuração de paginação (injetado automaticamente)
     * @param authentication - Usuário autenticado
     * @return Page<TaskResponseDTO> com metadados de paginação
     *
     * @PageableDefault:
     * - Define valores padrão se não especificados na URL
     * - size = 10: 10 tarefas por página
     * - sort = createdAt, DESC: ordena por data de criação (mais recente primeiro)
     *
     * Pageable:
     * - Spring converte query params em objeto Pageable
     * - ?page=0 → primeira página (zero-indexed)
     * - ?size=20 → 20 itens por página
     * - ?sort=title,asc → ordena por título ascendente
     * - ?sort=status,asc&sort=createdAt,desc → múltiplas ordenações
     *
     * Page<T>:
     * - Retorna:
     *   - content: lista de tarefas
     *   - totalElements: total de tarefas
     *   - totalPages: total de páginas
     *   - number: página atual
     *   - size: itens por página
     *   - first/last: flags de primeira/última página
     *
     * Exemplo de resposta:
     * {
     *   "content": [ {...}, {...} ],
     *   "pageable": { "pageNumber": 0, "pageSize": 10 },
     *   "totalElements": 25,
     *   "totalPages": 3,
     *   "last": false,
     *   "first": true
     * }
     */
    @GetMapping("/paginated")
    @Operation(
            summary = "Listar tarefas com paginação",
            description = "Retorna tarefas do usuário com paginação e ordenação"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de tarefas retornada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            )
    })
    public ResponseEntity<Page<TaskResponseDTO>> getTasksPaginated(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        Page<TaskResponseDTO> tasks = taskService.findAllByUserId(userId, pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Busca tarefas com filtros (status e busca por título).
     *
     * GET /api/tasks/search?status=PENDING&searchTerm=compras
     *
     * @param status - Filtro por status (opcional)
     * @param searchTerm - Busca no título (opcional)
     * @param pageable - Paginação
     * @param authentication - Usuário autenticado
     * @return Page<TaskResponseDTO>
     *
     * @RequestParam(required = false):
     * - Parâmetro opcional
     * - Se não informado, valor é null
     *
     * Exemplos:
     * - /api/tasks/search → todas as tarefas
     * - /api/tasks/search?status=PENDING → só pendentes
     * - /api/tasks/search?searchTerm=compras → título contém "compras"
     * - /api/tasks/search?status=COMPLETED&searchTerm=café → combinado
     *
     * @Parameter (Swagger):
     * - Documenta parâmetros da query string
     * - description: explica o parâmetro
     * - example: mostra exemplo de uso
     */
    @GetMapping("/search")
    @Operation(
            summary = "Buscar tarefas com filtros",
            description = "Busca tarefas por status e/ou termo no título"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarefas encontradas"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            )
    })
    public ResponseEntity<Page<TaskResponseDTO>> searchTasks(
            @Parameter(description = "Filtro por status (PENDING, IN_PROGRESS, COMPLETED)")
            @RequestParam(required = false) TaskStatus status,

            @Parameter(description = "Busca no título da tarefa", example = "compras")
            @RequestParam(required = false) String searchTerm,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,

            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        Page<TaskResponseDTO> tasks = taskService.findWithFilters(userId, status, searchTerm, pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Busca uma tarefa específica por ID.
     *
     * GET /api/tasks/{id}
     *
     * @param id - ID da tarefa
     * @param authentication - Usuário autenticado
     * @return TaskResponseDTO
     *
     * @PathVariable:
     * - Extrai valor da URL
     * - /api/tasks/123 → id = 123
     *
     * Segurança:
     * - TaskService valida que tarefa pertence ao usuário
     * - Se não pertencer → ForbiddenException (HTTP 403)
     *
     * Swagger:
     * - 200: tarefa encontrada
     * - 401: não autenticado
     * - 403: tarefa de outro usuário
     * - 404: tarefa não existe
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar tarefa por ID",
            description = "Retorna uma tarefa específica do usuário autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarefa encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar esta tarefa"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa não encontrada"
            )
    })
    public ResponseEntity<TaskResponseDTO> getTaskById(
            @Parameter(description = "ID da tarefa", example = "1")
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        TaskResponseDTO task = taskService.findById(id, userId);
        return ResponseEntity.ok(task);
    }

    /**
     * Atualiza uma tarefa existente.
     *
     * PUT /api/tasks/{id}
     *
     * @param id - ID da tarefa
     * @param dto - Novos dados da tarefa
     * @param authentication - Usuário autenticado
     * @return TaskResponseDTO atualizado
     *
     * Fluxo:
     * 1. Valida que tarefa pertence ao usuário
     * 2. Atualiza campos
     * 3. Salva no banco
     * 4. Retorna tarefa atualizada
     *
     * Swagger:
     * - 200: tarefa atualizada
     * - 400: dados inválidos
     * - 401: não autenticado
     * - 403: tarefa de outro usuário
     * - 404: tarefa não existe
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar tarefa",
            description = "Atualiza uma tarefa existente do usuário autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarefa atualizada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para atualizar esta tarefa"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa não encontrada"
            )
    })
    public ResponseEntity<TaskResponseDTO> updateTask(
            @Parameter(description = "ID da tarefa", example = "1")
            @PathVariable Long id,

            @Valid @RequestBody TaskRequestDTO dto,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        TaskResponseDTO task = taskService.updateTask(id, dto, userId);
        return ResponseEntity.ok(task);
    }

    /**
     * Deleta uma tarefa.
     *
     * DELETE /api/tasks/{id}
     *
     * @param id - ID da tarefa
     * @param authentication - Usuário autenticado
     * @return HTTP 204 No Content (sem corpo de resposta)
     *
     * ResponseEntity.noContent():
     * - HTTP 204
     * - Indica sucesso, mas sem dados para retornar
     * - Padrão REST para DELETE bem-sucedido
     *
     * Alternativa:
     * - ResponseEntity.ok() → HTTP 200 (também válido)
     *
     * Swagger:
     * - 204: tarefa deletada
     * - 401: não autenticado
     * - 403: tarefa de outro usuário
     * - 404: tarefa não existe
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar tarefa",
            description = "Remove uma tarefa do usuário autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Tarefa deletada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para deletar esta tarefa"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa não encontrada"
            )
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID da tarefa", example = "1")
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Conta tarefas por status.
     *
     * GET /api/tasks/count?status=PENDING
     *
     * @param status - Status para contar
     * @param authentication - Usuário autenticado
     * @return Quantidade de tarefas
     *
     * Uso:
     * - Dashboard: "Você tem 5 tarefas pendentes"
     * - Estatísticas do usuário
     *
     * Exemplo de resposta:
     * 5
     */
    @GetMapping("/count")
    @Operation(
            summary = "Contar tarefas por status",
            description = "Retorna quantidade de tarefas do usuário em um status específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contagem retornada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado"
            )
    })
    public ResponseEntity<Long> countTasksByStatus(
            @Parameter(description = "Status para contar", example = "PENDING")
            @RequestParam TaskStatus status,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuthentication(authentication);
        long count = taskService.countByStatus(userId, status);
        return ResponseEntity.ok(count);
    }
}