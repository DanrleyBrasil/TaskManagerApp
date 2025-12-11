package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma TAREFA no sistema.
 * Mapeia a tabela "tasks" do banco de dados.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "user") // Evita recursão infinita (User <-> Task)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título da tarefa
     */
    @Column(length = 200, nullable = false)
    private String title;

    /**
     * Descrição detalhada
     *
     * @Column(columnDefinition = "TEXT"):
     * - Força o tipo TEXT no banco (tamanho ilimitado)
     * - Alternativa: @Lob (depende do dialeto do banco)
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Status da tarefa
     *
     * Vamos criar um ENUM para isso (TaskStatus)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TaskStatus status = TaskStatus.PENDING; // ← Valor padrão

    /**
     * Relacionamento Muitos-para-Um com User
     *
     * @ManyToOne:
     * - Muitas tasks pertencem a um usuário
     * - Um usuário tem várias tasks
     *
     * fetch = FetchType.LAZY:
     * - Só carrega o usuário quando task.getUser() for chamado
     * - Performance: não carrega User desnecessariamente
     *
     * @JoinColumn:
     * - Define a coluna de foreign key (user_id)
     * - nullable = false: toda task DEVE ter um usuário
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Se status não foi definido, usa PENDING
        if (this.status == null) {
            this.status = TaskStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}