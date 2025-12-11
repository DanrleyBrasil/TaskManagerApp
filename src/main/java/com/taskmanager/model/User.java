package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade que representa um USUÁRIO no sistema.
 * Mapeia a tabela "users" do banco de dados.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"password", "roles", "tasks"}) // Não exibe campos sensíveis
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome de usuário (login)
     *
     * unique = true: garante que não teremos usuários duplicados
     */
    @Column(length = 50, nullable = false, unique = true)
    private String username;

    /**
     * Email do usuário
     */
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    /**
     * Senha criptografada (BCrypt)
     *
     * ATENÇÃO: NUNCA retorne senha em APIs!
     * Vamos tratar isso nos DTOs
     */
    @Column(length = 255, nullable = false)
    private String password;

    /**
     * Relacionamento Muitos-para-Muitos com Role
     *
     * @ManyToMany:
     * - Um usuário pode ter vários roles
     * - Um role pode pertencer a vários usuários
     *
     * fetch = FetchType.EAGER:
     * - Carrega os roles JUNTO com o usuário (em 1 query)
     * - Alternativa: LAZY (carrega só quando acessar user.getRoles())
     * - Uso EAGER porque sempre precisamos dos roles para autorização
     *
     * @JoinTable:
     * - Define a tabela intermediária (user_roles)
     * - joinColumns: coluna que aponta para esta entidade (user_id)
     * - inverseJoinColumns: coluna que aponta para a outra entidade (role_id)
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Relacionamento Um-para-Muitos com Task
     *
     * @OneToMany:
     * - Um usuário pode ter várias tarefas
     * - Uma tarefa pertence a apenas um usuário
     *
     * mappedBy = "user":
     * - Indica que o lado "dono" do relacionamento é Task
     * - Task tem a coluna user_id (foreign key)
     * - User não tem coluna extra, apenas acessa via JOIN
     *
     * cascade = CascadeType.ALL:
     * - Operações em cascata: salvar/deletar User afeta Tasks
     * - Se deletar User, deleta suas Tasks automaticamente
     *
     * orphanRemoval = true:
     * - Se remover Task da lista user.getTasks(), deleta do banco
     *
     * fetch = FetchType.LAZY:
     * - Só carrega tasks quando acessar user.getTasks()
     * - Performance: não carrega todas as tasks sempre
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Task> tasks = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Método executado ANTES de atualizar no banco
     *
     * @PreUpdate:
     * - Callback do JPA
     * - Executado automaticamente antes de UPDATE
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Método auxiliar para adicionar role ao usuário
     * Mantém consistência do relacionamento bidirecional
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Método auxiliar para adicionar task ao usuário
     * Mantém consistência do relacionamento bidirecional
     */
    public void addTask(Task task) {
        this.tasks.add(task);
        task.setUser(this); // ← Importante! Sincroniza o outro lado
    }
}