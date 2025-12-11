package com.taskmanager.service;

import com.taskmanager.dto.TaskRequestDTO;
import com.taskmanager.dto.TaskResponseDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para TaskService.
 *
 *
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;
    private TaskRequestDTO taskRequestDTO;

    /**
     * Executado ANTES de CADA teste.
     *
     * Cria objetos "limpos" para cada teste.
     */
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setEmail("admin@email.com");
        user.setPassword("adminTeste");

        task = new Task();
        task.setId(1L);
        task.setTitle("Comprar Café");
        task.setDescription("1kg de café torrado");
        task.setStatus(TaskStatus.PENDING);
        task.setUser(user);

        taskRequestDTO = new TaskRequestDTO();
        taskRequestDTO.setTitle("Comprar Café");
        taskRequestDTO.setDescription("1kg de café torrado");
        taskRequestDTO.setStatus(TaskStatus.PENDING);
    }


    @Test
    @DisplayName("Deve criar tarefa com sucesso quando dados válidos")
    void shouldCreateTask_WhenValidDataProvided(){
        // Arrange
        // Configura comportamento do userRepository
        when(userService.findById(1L)).thenReturn(user);
        // Configura comportamento do task repository
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // ACT

        TaskResponseDTO result = taskService.createTask(taskRequestDTO,1L);

        // ASSERT

        assertNotNull(result);
        assertEquals("Comprar Chocolate", result.getTitle());
        assertEquals("1kg de café torrado", result.getDescription());
        assertEquals(TaskStatus.PENDING, result.getStatus());

        // VERIFY
        verify(userService, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));

    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não existe")
    void shouldThrowResourceNotFoundException_WhenUserNotFound() {
        // ARRANGE

        // TODO: Configure o mock para retornar VAZIO quando buscar usuário 999L
        // Dica: Use Optional.empty() em vez de Optional.of(user)
        when(userService.findById(999L)).thenThrow(new ResourceNotFoundException("Usuário não encontrado com ID: 999"));

        // ACT & ASSERT

        // TODO: Qual exceção deve ser lançada?
        // Dica: Olhe o JavaDoc do cenário - está no nome do método!
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,  // TODO: Coloque a classe da exceção aqui
                () -> taskService.createTask(taskRequestDTO, 999L)  // TODO: Qual userId usar? (o que não existe)
        );

        // TODO: Verifique a mensagem da exceção
        // Dica: Olhe no TaskService.java o que ele lança quando não encontra
        assertEquals("Usuário não encontrado com ID: 999", exception.getMessage());

        // VERIFY

        // TODO: O método save() deve ser chamado?
        // Dica: Se deu exceção ANTES de salvar, save() foi chamado?
        verify(taskRepository, never()).save(any());
    }


    @Test
    @DisplayName("Deve usar status PENDING quando status é null")
    void shouldUseDefaultStatus_WhenStatusIsNull() {
        // ARRANGE

        // TODO: Setar o status do DTO como NULL
        // Dica: taskRequestDTO.setStatus(???)
        taskRequestDTO.setStatus(null);

        // TODO: Configure o mock do userService
        // Dica: Usuário EXISTE (retorna o user que criamos no setUp)
        // Qual método? findById com qual parâmetro?
        when(userService.findById(1L)).thenReturn(user);

        // TODO: Configure o mock do taskRepository
        // Dica: Quando save() for chamado, retorna a task
        // MAS... precisamos garantir que a task salva tem status PENDING!
        // Por enquanto, só configure o retorno básico
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // ACT

        // TODO: Chamar o método createTask
        // Qual DTO usar? Qual userId?
        TaskResponseDTO result = taskService.createTask(taskRequestDTO, 1L);

        // ASSERT

        // TODO: Verificar que resultado NÃO é null
        assertNotNull(result);

        // TODO: Verificar que o STATUS do resultado é PENDING
        // Dica: result.getStatus() deve ser igual a TaskStatus.PENDING
        assertEquals(TaskStatus.PENDING, result.getStatus());

        // TODO: Verificar título e descrição também (garantir que resto está OK)
        assertEquals("Comprar Café", result.getTitle());
        assertEquals("1kg de café torrado", result.getDescription());

        // VERIFY

        // TODO: Verificar que findById foi chamado
        verify(userService, times(1)).findById(1L);

        // TODO: Verificar que save foi chamado E que a Task tinha status PENDING
        // DESAFIO EXTRA: Use argThat() para verificar o STATUS da Task
        // Dica: verify(repo).save(argThat(task -> task.getStatus() == TaskStatus.PENDING))
        verify(taskRepository, times(1)).save(
                argThat(task -> task.getStatus() == TaskStatus.PENDING)
        );
    }

    @Test
    @DisplayName("Deve criar tarefa sem description quando description é null")
    void shouldCreateTask_WhenDescriptionIsNull() {
        // ARRANGE

        // TODO: Setar description como NULL no DTO
        taskRequestDTO.setDescription(null);

        // TODO: Também setar description como NULL na task que será retornada
        // Dica: A task foi criada no setUp(), precisa ajustar ela aqui
        task.setDescription(null);

        // TODO: Configure mocks (igual cenário 1!)
        // userService.findById retorna user
        when(userService.findById(1L)).thenReturn(user);

        // taskRepository.save retorna task (que agora tem description null)
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        // ACT

        // TODO: Chamar createTask
        TaskResponseDTO result = taskService.createTask(taskRequestDTO, 1L);

        // ASSERT

        // TODO: Verificar que resultado não é null
        assertNotNull(result);

        // TODO: Verificar que DESCRIPTION é NULL (isso que é diferente!)
        assertNull(task.getDescription());  // ← Use assertNull em vez de assertEquals!

        // TODO: Verificar que título e status estão OK (resto não mudou)
        assertEquals("Comprar Café", result.getTitle());
        assertEquals(TaskStatus.PENDING, result.getStatus());

        // VERIFY

        // TODO: Verificar chamadas (igual sempre!)
        verify(userService, times(1)).findById(1l);
        verify(taskRepository, times(1)).save(any(Task.class));
    }


}