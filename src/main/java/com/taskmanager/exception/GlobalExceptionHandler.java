package com.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manipulador global de exceções.
 *
 * @RestControllerAdvice:
 * - Intercepta todas as exceptions lançadas nos Controllers
 * - Permite tratar exceptions de forma centralizada
 * - Retorna ResponseEntity com erro padronizado
 *
 * SEM GlobalExceptionHandler:
 * - Cada Controller precisa tratar suas exceções
 * - Código duplicado (try-catch em todo lugar)
 * - Respostas de erro inconsistentes
 *
 * COM GlobalExceptionHandler:
 * - Tratamento centralizado
 * - Código limpo nos Controllers
 * - Respostas de erro padronizadas
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata ResourceNotFoundException.
     *
     * @ExceptionHandler:
     * - Define qual exception este método trata
     * - Spring chama automaticamente quando exception é lançada
     *
     * HTTP Status: 404 NOT FOUND
     *
     * Exemplo:
     * throw new ResourceNotFoundException("Usuário não encontrado");
     * → Cliente recebe HTTP 404 com JSON de erro
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Trata BadRequestException.
     *
     * HTTP Status: 400 BAD REQUEST
     *
     * Exemplo:
     * throw new BadRequestException("Username já está em uso");
     * → Cliente recebe HTTP 400 com JSON de erro
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata ForbiddenException.
     *
     * HTTP Status: 403 FORBIDDEN
     *
     * Exemplo:
     * throw new ForbiddenException("Você não tem permissão");
     * → Cliente recebe HTTP 403 com JSON de erro
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Trata erros de validação do Bean Validation.
     *
     * MethodArgumentNotValidException:
     * - Lançada quando @Valid falha nos DTOs
     * - Contém lista de erros de validação
     *
     * HTTP Status: 400 BAD REQUEST
     *
     * Exemplo:
     * POST /api/auth/register
     * {
     *   "username": "",  ← Viola @NotBlank
     *   "email": "invalido",  ← Viola @Email
     *   "password": "123"  ← Viola @Pattern
     * }
     *
     * Retorna:
     * {
     *   "status": 400,
     *   "message": "Erro de validação",
     *   "errors": [
     *     "Username é obrigatório",
     *     "Email deve ser válido",
     *     "Senha deve conter: letra maiúscula, minúscula, número e caractere especial"
     *   ]
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        List<String> errors = new ArrayList<>();

        /**
         * Extrai todos os erros de validação
         *
         * ex.getBindingResult().getAllErrors():
         * - Retorna lista de ObjectError
         * - Cada erro representa uma validação que falhou
         *
         * FieldError:
         * - Tipo específico de ObjectError
         * - Contém: campo, mensagem, valor rejeitado
         */
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Erro de validação",
                request.getDescription(false).replace("uri=", ""),
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Trata erro de credenciais inválidas (login).
     *
     * BadCredentialsException:
     * - Lançada pelo Spring Security quando username/password estão errados
     * - AuthenticationManager.authenticate() lança essa exception
     *
     * HTTP Status: 401 UNAUTHORIZED
     *
     * Exemplo:
     * POST /api/auth/login
     * {
     *   "usernameOrEmail": "joao",
     *   "password": "senhaErrada"
     * }
     * → HTTP 401 com mensagem "Credenciais inválidas"
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Credenciais inválidas",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Trata UsernameNotFoundException.
     *
     * Lançada pelo UserService.loadUserByUsername() quando usuário não existe
     *
     * HTTP Status: 401 UNAUTHORIZED
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Usuário não encontrado",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Trata qualquer outra exception não tratada.
     *
     * Fallback:
     * - Captura exceptions que não têm handler específico
     * - Evita que erros internos vazem para o cliente
     * - Retorna mensagem genérica
     *
     * HTTP Status: 500 INTERNAL SERVER ERROR
     *
     * IMPORTANTE:
     * - Em produção, NÃO exponha detalhes do erro interno!
     * - Log o erro completo para debug
     * - Retorne mensagem genérica para o cliente
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request
    ) {
        // Log o erro completo (para debug)
        System.err.println("Erro não tratado: " + ex.getClass().getName());
        ex.printStackTrace();

        // Retorna mensagem genérica (não expõe detalhes internos)
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Erro interno do servidor",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}