package com.taskmanager.repository;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para acesso aos dados de Task.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Busca todas as tarefas de um usuário específico.
     *
     * QUERY GERADA:
     * SELECT * FROM tasks WHERE user_id = ?
     *
     * Uso: listar "minhas tarefas"
     */
    List<Task> findByUserId(Long userId);

    /**
     * Busca todas as tarefas de um usuário com paginação.
     *
     * Pageable:
     * - Interface do Spring para paginação
     * - Contém: página atual, tamanho, ordenação
     *
     * Page<Task>:
     * - Retorna: lista de tasks + metadados (total, páginas, etc)
     *
     * Uso:
     * Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
     * Page<Task> page = taskRepository.findByUserId(userId, pageable);
     *
     * QUERY GERADA:
     * SELECT * FROM tasks WHERE user_id = ?
     * ORDER BY created_at DESC
     * LIMIT 10 OFFSET 0
     */
    Page<Task> findByUserId(Long userId, Pageable pageable);

    /**
     * Busca tarefas de um usuário por status.
     *
     * QUERY GERADA:
     * SELECT * FROM tasks WHERE user_id = ? AND status = ?
     *
     * Uso: listar "minhas tarefas PENDING"
     */
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);

    /**
     * Busca tarefas de um usuário por status com paginação.
     */
    Page<Task> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);

    /**
     * Busca uma tarefa específica de um usuário.
     *
     * QUERY GERADA:
     * SELECT * FROM tasks WHERE id = ? AND user_id = ?
     *
     * Uso: garantir que usuário só acessa SUA tarefa
     * Segurança: evita que user_id=1 acesse task de user_id=2
     */
    Optional<Task> findByIdAndUserId(Long id, Long userId);

    /**
     * Busca tarefas criadas após uma data específica.
     *
     * QUERY GERADA:
     * SELECT * FROM tasks WHERE user_id = ? AND created_at > ?
     *
     * Uso: "tarefas criadas esta semana"
     */
    List<Task> findByUserIdAndCreatedAtAfter(Long userId, LocalDateTime date);

    /**
     * Conta quantas tarefas um usuário tem por status.
     *
     * QUERY GERADA:
     * SELECT COUNT(*) FROM tasks WHERE user_id = ? AND status = ?
     *
     * Uso: dashboard ("você tem 5 tarefas pendentes")
     */
    long countByUserIdAndStatus(Long userId, TaskStatus status);

    /**
     * Conta total de tarefas de um usuário.
     *
     * QUERY GERADA:
     * SELECT COUNT(*) FROM tasks WHERE user_id = ?
     */
    long countByUserId(Long userId);

    /**
     * Busca tarefas com título contendo um texto (busca parcial).
     *
     * QUERY GERADA:
     * SELECT * FROM tasks WHERE user_id = ? AND title LIKE %?%
     *
     * Uso: barra de busca ("procurar 'compras'")
     *
     * IgnoreCase:
     * - Busca case-insensitive
     * - "Compras", "compras", "COMPRAS" → todos encontrados
     */
    List<Task> findByUserIdAndTitleContainingIgnoreCase(Long userId, String title);

    /**
     * Query customizada: busca tarefas com filtros múltiplos.
     *
     * COALESCE:
     * - Retorna primeiro valor não-nulo
     * - Usado para parâmetros opcionais
     *
     * Exemplo:
     * - Se status = null, busca todas as tarefas (qualquer status)
     * - Se status != null, filtra por esse status
     */
    @Query("""
        SELECT t FROM Task t 
        WHERE t.user.id = :userId 
        AND (:status IS NULL OR t.status = :status)
        AND (:searchTerm IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        ORDER BY t.createdAt DESC
        """)
    Page<Task> findByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    /**
     * Deleta todas as tarefas de um usuário.
     *
     * QUERY GERADA:
     * DELETE FROM tasks WHERE user_id = ?
     *
     * ATENÇÃO: Use com cuidado!
     * Geralmente não precisa porque cascade já cuida disso
     */
    void deleteByUserId(Long userId);
}