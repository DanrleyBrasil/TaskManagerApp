package com.taskmanager.service;

import com.taskmanager.dto.TaskRequestDTO;
import com.taskmanager.dto.TaskResponseDTO;
import com.taskmanager.exception.ForbiddenException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsável pela lógica de negócio de tarefas.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    /**
     * Cria uma nova tarefa.
     *
     * @param dto - Dados da tarefa
     * @param userId - ID do usuário dono da tarefa
     * @return TaskResponseDTO
     */
    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO dto, Long userId) {
        // Busca usuário
        User user = userService.findById(userId);

        // Cria tarefa
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : TaskStatus.PENDING);
        task.setUser(user);

        // Salva no banco
        Task savedTask = taskRepository.save(task);

        // Retorna DTO
        return new TaskResponseDTO(savedTask);
    }

    /**
     * Busca todas as tarefas de um usuário.
     *
     * @param userId - ID do usuário
     * @return Lista de TaskResponseDTO
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> findAllByUserId(Long userId) {
        return taskRepository.findByUserId(userId).stream()
                .map(TaskResponseDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Busca tarefas de um usuário com paginação.
     *
     * @param userId - ID do usuário
     * @param pageable - Configuração de paginação
     * @return Page<TaskResponseDTO>
     */
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> findAllByUserId(Long userId, Pageable pageable) {
        return taskRepository.findByUserId(userId, pageable)
                .map(TaskResponseDTO::new); // Converte cada Task em TaskResponseDTO
    }

    /**
     * Busca tarefas com filtros.
     *
     * @param userId - ID do usuário
     * @param status - Filtro por status (opcional)
     * @param searchTerm - Filtro por título (opcional)
     * @param pageable - Paginação
     * @return Page<TaskResponseDTO>
     */
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> findWithFilters(Long userId, TaskStatus status, String searchTerm, Pageable pageable) {
        return taskRepository.findByUserIdWithFilters(userId, status, searchTerm, pageable)
                .map(TaskResponseDTO::new);
    }

    /**
     * Busca uma tarefa específica.
     *
     * Validação de segurança:
     * - Garante que usuário só acessa SUA tarefa
     *
     * @param taskId - ID da tarefa
     * @param userId - ID do usuário
     * @return TaskResponseDTO
     * @throws ResourceNotFoundException se tarefa não existir
     * @throws ForbiddenException se tarefa não pertencer ao usuário
     */
    @Transactional(readOnly = true)
    public TaskResponseDTO findById(Long taskId, Long userId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tarefa não encontrada com ID: " + taskId)
                );

        return new TaskResponseDTO(task);
    }

    /**
     * Atualiza uma tarefa.
     *
     * @param taskId - ID da tarefa
     * @param dto - Novos dados
     * @param userId - ID do usuário
     * @return TaskResponseDTO atualizado
     * @throws ForbiddenException se tarefa não pertencer ao usuário
     */
    @Transactional
    public TaskResponseDTO updateTask(Long taskId, TaskRequestDTO dto, Long userId) {
        // Busca e valida propriedade
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tarefa não encontrada com ID: " + taskId)
                );

        // Atualiza campos
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }

        // Salva (JPA detecta mudanças automaticamente)
        Task updatedTask = taskRepository.save(task);

        return new TaskResponseDTO(updatedTask);
    }

    /**
     * Deleta uma tarefa.
     *
     * @param taskId - ID da tarefa
     * @param userId - ID do usuário
     * @throws ForbiddenException se tarefa não pertencer ao usuário
     */
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        // Busca e valida propriedade
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tarefa não encontrada com ID: " + taskId)
                );

        // Deleta
        taskRepository.delete(task);
    }

    /**
     * Conta tarefas por status.
     *
     * Uso: dashboard
     *
     * @param userId - ID do usuário
     * @param status - Status para contar
     * @return Quantidade de tarefas
     */
    @Transactional(readOnly = true)
    public long countByStatus(Long userId, TaskStatus status) {
        return taskRepository.countByUserIdAndStatus(userId, status);
    }

    /**
     * Busca tarefas por status.
     *
     * @param userId - ID do usuário
     * @param status - Status
     * @return Lista de TaskResponseDTO
     */
    @Transactional(readOnly = true)
    public List<TaskResponseDTO> findByStatus(Long userId, TaskStatus status) {
        return taskRepository.findByUserIdAndStatus(userId, status).stream()
                .map(TaskResponseDTO::new)
                .collect(Collectors.toList());
    }


}