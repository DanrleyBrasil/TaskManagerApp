package com.taskmanager.exception;

/**
 * Exception lançada quando o usuário não tem permissão.
 *
 * Casos de uso:
 * - Usuário tenta acessar tarefa de outro usuário
 * - USER tenta acessar endpoint de ADMIN
 * - Operação não permitida para o role atual
 *
 * HTTP Status: 403 FORBIDDEN
 *
 * DIFERENÇA:
 * - 401 UNAUTHORIZED: Não está autenticado (sem token/token inválido)
 * - 403 FORBIDDEN: Está autenticado, mas não tem permissão
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Construtor com mensagem customizada.
     *
     * Uso:
     * throw new ForbiddenException("Você não tem permissão para acessar esta tarefa");
     */
    public ForbiddenException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa.
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}