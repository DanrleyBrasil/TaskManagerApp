package com.taskmanager.repository;

import com.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para acesso aos dados de User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuário por username (usado no login).
     *
     * QUERY GERADA:
     * SELECT u FROM User u
     * LEFT JOIN FETCH u.roles
     * WHERE u.username = ?
     *
     * Por que FETCH?
     * - Carrega os roles junto (1 query só)
     * - Evita LazyInitializationException
     * - Performance: não faz N+1 queries
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca usuário por email.
     *
     * QUERY GERADA:
     * SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se existe usuário com o username.
     *
     * QUERY GERADA:
     * SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)
     *
     * Uso: validar se username já está em uso no cadastro
     */
    boolean existsByUsername(String username);

    /**
     * Verifica se existe usuário com o email.
     *
     * QUERY GERADA:
     * SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     *
     * Uso: validar se email já está em uso no cadastro
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuário por username OU email (útil para login flexível).
     *
     * QUERY GERADA:
     * SELECT * FROM users WHERE username = ? OR email = ?
     *
     * Uso: permitir login com username ou email
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Query customizada usando JPQL.
     *
     * @Query:
     * - Permite escrever queries JPQL/SQL customizadas
     * - Útil quando Query Method não é suficiente
     *
     * JPQL vs SQL:
     * - JPQL usa nomes de classes/atributos Java (User, username)
     * - SQL usa nomes de tabelas/colunas do banco (users, username)
     *
     * JOIN FETCH:
     * - Carrega relacionamentos em uma única query (eager)
     * - Evita N+1 problem
     * - Performance: 1 query com JOIN em vez de 2 queries
     *
     * @Param:
     * - Nomeia os parâmetros da query
     * - Mais legível que ?1, ?2, ?3
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Busca usuário por email com roles (eager loading).
     *
     * Uso: quando precisar do usuário E seus roles juntos
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);
}