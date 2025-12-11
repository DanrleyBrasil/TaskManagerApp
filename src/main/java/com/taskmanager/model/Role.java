package com.taskmanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que representa um ROLE (perfil de usuário) no sistema.
 * Mapeia a tabela "roles" do banco de dados.
 */
@Entity // Indica que esta classe é uma entidade JPA
@Table(name = "roles") // Nome da tabela no banco (explícito)
@Getter // Lombok: gera getters automaticamente
@Setter // Lombok: gera setters automaticamente
@NoArgsConstructor // Lombok: gera construtor vazio (JPA precisa)
@AllArgsConstructor // Lombok: gera construtor com todos os campos
@EqualsAndHashCode(of = "id") // Lombok: equals/hashCode baseado no ID
@ToString(of = {"id", "name"}) // Lombok: toString sem campos sensíveis
public class Role {

    /**
     * ID único do role (chave primária)
     *
     * @GeneratedValue(strategy = IDENTITY):
     * - IDENTITY: banco gera o ID automaticamente (SERIAL/BIGSERIAL)
     * - Alternativas: AUTO, SEQUENCE, TABLE
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome do role (ex: ROLE_USER, ROLE_ADMIN)
     *
     * @Enumerated(EnumType.STRING):
     * - STRING: salva o nome do enum ("ROLE_USER")
     * - ORDINAL: salvaria número (0, 1, 2) - NÃO USE! Se mudar ordem, quebra tudo
     *
     * @Column:
     * - length = 50: tamanho máximo (bate com VARCHAR(50) do banco)
     * - nullable = false: campo obrigatório
     * - unique = true: não pode ter role duplicado
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false, unique = true)
    private RoleName name;

    /**
     * Descrição do que esse role pode fazer
     *
     * nullable = true (padrão): campo opcional
     */
    @Column(length = 255)
    private String description;

    /**
     * Data de criação do role
     *
     * @Column(updatable = false):
     * - Impede que JPA atualize esse campo em UPDATEs
     * - created_at só é setado uma vez (na criação)
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Método executado ANTES de persistir no banco
     *
     * @PrePersist:
     * - Callback do JPA
     * - Executado automaticamente antes do INSERT
     * - Útil para setar valores padrão
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}