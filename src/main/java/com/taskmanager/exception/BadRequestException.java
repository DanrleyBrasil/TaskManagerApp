package com.taskmanager.exception;

/**
 * Exception lançada quando a requisição é inválida.
 *
 * Casos de uso:
 * - Username já existe no cadastro
 * - Email já está em uso
 * - Dados de entrada inválidos (além das validações do Bean Validation)
 * - Regra de negócio violada
 *
 * HTTP Status: 400 BAD REQUEST
 */
public class BadRequestException extends RuntimeException {

    /**
     * Construtor com mensagem customizada.
     *
     * Uso:
     * throw new BadRequestException("Username já está em uso");
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa.
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}