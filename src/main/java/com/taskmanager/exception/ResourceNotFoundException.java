package com.taskmanager.exception;

/**
 * Exception lançada quando um recurso não é encontrado.
 *
 * Casos de uso:
 * - Buscar usuário por ID que não existe
 * - Buscar tarefa que não existe
 * - Buscar role que não existe
 *
 * HTTP Status: 404 NOT FOUND
 *
 * RuntimeException:
 * - Não precisa declarar no método (throws)
 * - Spring trata automaticamente
 * - Rollback automático em transações
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construtor com mensagem customizada.
     *
     * Uso:
     * throw new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa.
     *
     * Uso: quando uma exception causou esta
     * throw new ResourceNotFoundException("Erro ao buscar usuário", ex);
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}