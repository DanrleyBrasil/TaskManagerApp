package com.taskmanager.model;

/**
 * ENUM que define os possíveis status de uma tarefa
 */
public enum TaskStatus {
    /**
     * Tarefa criada, aguardando início
     */
    PENDING,

    /**
     * Tarefa em andamento
     */
    IN_PROGRESS,

    /**
     * Tarefa concluída
     */
    COMPLETED
}