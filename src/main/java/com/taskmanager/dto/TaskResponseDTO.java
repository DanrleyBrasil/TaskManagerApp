package com.taskmanager.dto;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de resposta com dados da tarefa.
 *
 * Inclui informações básicas do usuário dono da tarefa.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Informações do usuário dono da tarefa
     *
     * Nested DTO: DTO dentro de DTO
     * Evita expor toda a entidade User
     */
    private UserSummaryDTO user;

    /**
     * Construtor de conveniência para converter Task → TaskResponseDTO
     */
    public TaskResponseDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
        this.user = new UserSummaryDTO(task.getUser());
    }

    /**
     * DTO interno com informações resumidas do usuário
     *
     * Evita circular reference (Task → User → Tasks → User...)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDTO {
        private Long id;
        private String username;

        public UserSummaryDTO(com.taskmanager.model.User user) {
            this.id = user.getId();
            this.username = user.getUsername();
        }
    }
}