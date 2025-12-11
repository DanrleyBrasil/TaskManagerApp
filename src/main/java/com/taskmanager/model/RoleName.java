package com.taskmanager.model;

/**
 * ENUM que define os tipos de roles disponíveis no sistema.
 *
 * Por que usar ENUM?
 * - Type safety: Impossível usar valor inválido
 * - Documentação: IDE sugere apenas valores válidos
 * - Manutenção: Adicionar novo role = adicionar aqui
 */
public enum RoleName {
    /**
     * Role de usuário comum
     * Permissões: CRUD de suas próprias tarefas
     */
    ROLE_USER,

    /**
     * Role de administrador
     * Permissões: Acesso total ao sistema
     */
    ROLE_ADMIN
}