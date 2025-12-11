package com.taskmanager.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Objeto de resposta padronizado para erros.
 *
 * Retornado em todas as respostas de erro (4xx, 5xx).
 *
 * Exemplo de resposta JSON:
 * {
 *   "timestamp": "2025-12-10T15:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Usuário não encontrado com ID: 123",
 *   "path": "/api/users/123"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Timestamp do erro (quando aconteceu)
     */
    private LocalDateTime timestamp;

    /**
     * Status HTTP (400, 404, 500, etc)
     */
    private int status;

    /**
     * Nome do erro ("Bad Request", "Not Found", etc)
     */
    private String error;

    /**
     * Mensagem descritiva do erro
     */
    private String message;

    /**
     * Path da requisição que causou o erro
     */
    private String path;

    /**
     * Lista de erros de validação (para Bean Validation)
     *
     * Exemplo:
     * "errors": [
     *   "Username é obrigatório",
     *   "Email deve ser válido"
     * ]
     */
    private List<String> errors;

    /**
     * Construtor simplificado (sem lista de erros)
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}