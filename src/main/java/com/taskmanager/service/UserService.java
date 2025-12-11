package com.taskmanager.service;

import com.taskmanager.dto.UserResponseDTO;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.Role;
import com.taskmanager.model.RoleName;
import com.taskmanager.model.User;
import com.taskmanager.repository.RoleRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsável pela lógica de negócio de usuários.
 *
 * @RequiredArgsConstructor (Lombok):
 * - Gera construtor com campos final
 * - Injeta dependências automaticamente (melhor que @Autowired)
 *
 * UserDetailsService:
 * - Interface do Spring Security
 * - Usada para carregar dados do usuário durante autenticação
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    /**
     * Injeção de dependências
     *
     * final + @RequiredArgsConstructor:
     * - Spring injeta automaticamente no construtor
     * - Imutável (não pode ser alterado depois)
     * - Mais testável (pode passar mocks no construtor)
     */
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Método do UserDetailsService usado pelo Spring Security.
     *
     * Chamado automaticamente durante o login:
     * 1. Spring Security recebe username + password
     * 2. Chama loadUserByUsername(username)
     * 3. Compara password do UserDetails com o enviado
     * 4. Se bater → autenticado!
     *
     * @param username - Username ou email
     * @return UserDetails com dados do usuário
     * @throws UsernameNotFoundException se usuário não existir
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        /**
         * Busca usuário por username OU email
         *
         * Por que buscar com roles?
         * - Spring Security precisa dos roles para autorização
         * - JOIN FETCH evita LazyInitializationException
         */
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseGet(() ->
                        userRepository.findByEmailWithRoles(username)
                                .orElseThrow(() ->
                                        new UsernameNotFoundException("Usuário não encontrado: " + username)
                                )
                );

        /**
         * Converte roles para GrantedAuthority
         *
         * Spring Security espera Collection<GrantedAuthority>
         *
         * Stream API:
         * - user.getRoles() → Set<Role>
         * - .stream() → Stream<Role>
         * - .map() → transforma Role em SimpleGrantedAuthority
         * - .collect() → volta para Set
         */
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());

        /**
         * Retorna UserDetails
         *
         * org.springframework.security.core.userdetails.User:
         * - Implementação padrão de UserDetails
         * - Construtor: (username, password, authorities)
         *
         * Spring Security usa isso para:
         * - Autenticação: comparar senha
         * - Autorização: verificar roles
         */
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    /**
     * Busca usuário por username.
     *
     * @Transactional(readOnly = true):
     * - Otimização: informa que não vai modificar dados
     * - Performance: banco pode usar otimizações de leitura
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado: " + username)
                );
    }

    /**
     * Busca usuário por email.
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado: " + email)
                );
    }

    /**
     * Busca usuário por ID.
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuário não encontrado com ID: " + id)
                );
    }

    /**
     * Lista todos os usuários (apenas ADMIN).
     *
     * Converte entidades para DTOs (sem senha!)
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::new) // Converte User → UserResponseDTO
                .collect(Collectors.toList());
    }

    /**
     * Verifica se username já existe.
     *
     * Uso: validação no cadastro
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Verifica se email já existe.
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Salva usuário no banco.
     *
     * @Transactional:
     * - Garante atomicidade (tudo ou nada)
     * - Se dar erro, faz rollback automático
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Atribui role USER a um usuário.
     *
     * Uso: cadastro de novos usuários
     * Todo usuário novo começa como USER
     */
    @Transactional
    public void assignUserRole(User user) {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role USER não encontrado no sistema")
                );

        user.addRole(userRole);
    }

    /**
     * Promove usuário para ADMIN.
     *
     * Uso: endpoint /api/admin/users/{id}/promote
     * Apenas ADMIN pode promover outros usuários
     */
    @Transactional
    public void promoteToAdmin(Long userId) {
        User user = findById(userId);

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role ADMIN não encontrado no sistema")
                );

        user.addRole(adminRole);
        userRepository.save(user);
    }

    /**
     * Deleta usuário.
     *
     * CASCADE:
     * - Deleta automaticamente suas tarefas (ON DELETE CASCADE)
     * - Remove da tabela user_roles
     */
    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }

    /**
     * Busca ID do usuário por username.
     *
     * Método auxiliar para outros services.
     *
     * @param username - Username do usuário
     * @return ID do usuário
     */
    @Transactional(readOnly = true)
    public Long getIdByUsername(String username) {
        User user = findByUsername(username); // Reutiliza método existente!
        return user.getId();
    }
}