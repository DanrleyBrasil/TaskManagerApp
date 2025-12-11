package com.taskmanager.dto;

import com.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criar ou atualizar uma tarefa.
 *
 * Usado tanto em POST /tasks quanto PUT /tasks/{id}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDTO {

    /**
     * Título da tarefa
     */
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    private String title;

    /**
     * Descrição da tarefa (opcional)
     *
     * Sem @NotBlank = campo opcional
     */
    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    private String description;

    /**
     * Status da tarefa (opcional ao criar)
     *
     * Se não informado, será PENDING por padrão (definido na entidade)
     *
     * Valores válidos: PENDING, IN_PROGRESS, COMPLETED
     * Qualquer outro valor → erro 400 automaticamente
     */
    private TaskStatus status;
}