package com.taskmanager.repository;

import com.taskmanager.model.Role;
import com.taskmanager.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para acesso aos dados de Role.
 *
 * @Repository:
 * - Marca como componente do Spring (gerenciado pelo container)
 * - Spring Data JPA cria a implementação automaticamente
 * - Tratamento de exceções de persistência
 *
 * JpaRepository<Role, Long>:
 * - Role: tipo da entidade
 * - Long: tipo da chave primária (ID)
 * - Herda métodos prontos: save(), findById(), findAll(), delete(), etc
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca um role pelo nome.
     *
     * QUERY GERADA AUTOMATICAMENTE:
     * SELECT * FROM roles WHERE name = ?
     *
     * Optional<Role>:
     * - Retorna Optional para evitar NullPointerException
     * - Se não encontrar, retorna Optional.empty()
     * - Uso: roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(...)
     *
     * CONVENÇÃO DE NOMES (Query Methods):
     * - findBy + NomeDoCampo
     * - Spring traduz automaticamente para SQL
     *
     * Exemplos de convenções:
     * - findByName()           → WHERE name = ?
     * - findByNameAndId()      → WHERE name = ? AND id = ?
     * - findByNameOrEmail()    → WHERE name = ? OR email = ?
     * - findByNameContaining() → WHERE name LIKE %?%
     * - findByIdGreaterThan()  → WHERE id > ?
     */
    Optional<Role> findByName(RoleName name);

    /**
     * Verifica se existe um role com o nome especificado.
     *
     * QUERY GERADA:
     * SELECT EXISTS(SELECT 1 FROM roles WHERE name = ?)
     *
     * Retorna boolean (true/false)
     *
     * Performance:
     * - Mais eficiente que findByName() quando só precisa saber se existe
     * - Não carrega o objeto completo, só retorna true/false
     */
    boolean existsByName(RoleName name);
}