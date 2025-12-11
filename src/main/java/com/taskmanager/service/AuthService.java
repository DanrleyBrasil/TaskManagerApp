package com.taskmanager.service;

import com.taskmanager.dto.JwtResponseDTO;
import com.taskmanager.dto.LoginRequestDTO;
import com.taskmanager.dto.RegisterRequestDTO;
import com.taskmanager.dto.UserResponseDTO;
import com.taskmanager.exception.BadRequestException;
import com.taskmanager.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsável pela autenticação e registro de usuários.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra um novo usuário no sistema.
     *
     * Fluxo:
     * 1. Valida se username/email já existem
     * 2. Criptografa senha com BCrypt
     * 3. Cria usuário
     * 4. Atribui role USER
     * 5. Salva no banco
     * 6. Retorna DTO (sem senha!)
     *
     * @param dto - Dados do cadastro
     * @return UserResponseDTO
     * @throws BadRequestException se username/email já existir
     */
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO dto) {
        // Validação 1: Username já existe?
        if (userService.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Username já está em uso");
        }

        // Validação 2: Email já existe?
        if (userService.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email já está em uso");
        }

        /**
         * Cria novo usuário
         *
         * passwordEncoder.encode():
         * - Criptografa senha com BCrypt
         * - Gera hash + salt único
         * - Resultado: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
         *
         * NUNCA salve senha em texto plano!
         */
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Atribui role USER (padrão para novos usuários)
        userService.assignUserRole(user);

        // Salva no banco
        User savedUser = userService.save(user);

        // Retorna DTO (sem senha!)
        return new UserResponseDTO(savedUser);
    }

    /**
     * Realiza login e retorna token JWT.
     *
     * Fluxo:
     * 1. AuthenticationManager valida credenciais
     * 2. Se válido, gera token JWT
     * 3. Retorna token + dados do usuário
     *
     * @param dto - Username/email e senha
     * @return JwtResponseDTO com token
     * @throws BadCredentialsException se credenciais inválidas
     */
    @Transactional(readOnly = true)
    public JwtResponseDTO login(LoginRequestDTO dto) {
        /**
         * AuthenticationManager.authenticate()
         *
         * Internamente:
         * 1. Chama UserService.loadUserByUsername()
         * 2. Compara senha com passwordEncoder.matches()
         * 3. Se inválido → lança BadCredentialsException
         * 4. Se válido → retorna Authentication
         *
         * UsernamePasswordAuthenticationToken:
         * - Objeto que carrega username + password
         * - Spring Security usa para autenticação
         */
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsernameOrEmail(),
                        dto.getPassword()
                )
        );

        /**
         * Seta autenticação no contexto do Spring Security
         *
         * SecurityContextHolder:
         * - Thread local que guarda autenticação
         * - Acessível em qualquer lugar da aplicação
         * - Uso: SecurityContextHolder.getContext().getAuthentication()
         */
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Gera token JWT
        String jwt = jwtService.generateToken(authentication);

        // Busca dados do usuário autenticado
        User user = userService.findByUsername(authentication.getName());

        /**
         * Extrai nomes dos roles
         *
         * authentication.getAuthorities():
         * - Retorna Collection<GrantedAuthority>
         * - Exemplo: [ROLE_USER, ROLE_ADMIN]
         */
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Retorna resposta com token
        return new JwtResponseDTO(jwt, user.getId(), user.getUsername(), user.getEmail(), roles);
    }

    /**
     * Retorna o usuário autenticado atual.
     *
     * Uso: GET /api/auth/me
     *
     * @return UserResponseDTO do usuário logado
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser() {
        // Pega autenticação do contexto
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Pega username
        String username = authentication.getName();

        // Busca usuário
        User user = userService.findByUsername(username);

        return new UserResponseDTO(user);
    }
}