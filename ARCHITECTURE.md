# 🏗️ Decisões Arquiteturais - Task Manager API

Este documento registra as principais decisões arquiteturais do projeto, explicando **O QUÊ**, **POR QUÊ** e **ALTERNATIVAS CONSIDERADAS**.

---

## 📋 Índice

1. [Arquitetura Geral](#1-arquitetura-geral)
2. [Escolha das Tecnologias](#2-escolha-das-tecnologias)
3. [Padrões de Projeto](#3-padrões-de-projeto)
4. [Segurança](#4-segurança)
5. [Persistência de Dados](#5-persistência-de-dados)
6. [Testes](#6-testes)
7. [Containerização](#7-containerização)

---

## 1. Arquitetura Geral

### ✅ Decisão: Arquitetura em Camadas (Layered Architecture)

**O que é:**
Separação da aplicação em camadas com responsabilidades bem definidas:
- **Controller** (Apresentação)
- **Service** (Lógica de Negócio)
- **Repository** (Acesso a Dados)
- **Model** (Entidades)

**Por que:**
1. **Separação de responsabilidades** - Cada camada tem um propósito claro
2. **Testabilidade** - Podemos testar cada camada isoladamente (mocks)
3. **Manutenibilidade** - Mudanças em uma camada não quebram outras
4. **Padrão consolidado** - Amplamente usado na indústria, fácil para novos devs entenderem
5. **Escalabilidade** - Fácil adicionar novas funcionalidades

**Alternativas consideradas:**
- ❌ **Clean Architecture / Hexagonal** - Mais complexa para uma POC simples, overhead desnecessário
- ❌ **Monolito sem camadas** - Dificulta testes e manutenção
- ❌ **Microserviços** - Overkill para um projeto de estudo, complexidade operacional alta

**Trade-offs:**
- ✅ Simplicidade vs ❌ Menos flexibilidade que Clean Architecture
- ✅ Fácil de entender vs ❌ Pode virar "monolito em camadas" se não bem organizado

---

## 2. Escolha das Tecnologias

### 2.1 Java 21

**Por que:**
- ✅ Versão LTS (Long Term Support)
- ✅ Virtual Threads (Project Loom) para melhor performance em I/O
- ✅ Pattern Matching e Record Classes (código mais limpo)
- ✅ Alinhado com o mercado atual

**Alternativas:**
- ❌ Java 17 - Versão LTS anterior, mas sem features modernas do 21
- ❌ Java 11 - Antiga demais, perdendo suporte
- ❌ Kotlin - Excelente, mas menos comum em vagas Jr/Pleno

---

### 2.2 Spring Boot 4

**Por que:**
- ✅ Framework mais usado no mercado Java (curva de aprendizado vale a pena)
- ✅ Produtividade alta (configuração mínima, convenções sensatas)
- ✅ Ecossistema maduro (Security, Data, Cloud, etc)

---

### 2.3 PostgreSQL

**Por que:**
- ✅ Banco relacional robusto e open-source
- ✅ Suporte a JSON (híbrido relacional + NoSQL)
- ✅ Excelente performance
- ✅ Amplamente usado em empresas

**Alternativas:**
- ❌ MySQL - Bom, mas PostgreSQL tem features mais avançadas
- ❌ H2 (em memória) - Só para testes, não para "produção"
- ❌ MongoDB - NoSQL não faz sentido para relacionamentos Task<->User

---

### 2.4 Flyway (Migrations)

**Por que:**
- ✅ Versionamento de banco de dados (como Git para BD)
- ✅ Histórico de mudanças rastreável
- ✅ Trabalho em equipe facilitado (todos aplicam mesmas migrations)
- ✅ Rollback seguro (você sabe exatamente o que mudou)
- ✅ Ambiente de dev igual ao de produção

**Alternativas:**
- ❌ Liquibase - Mais complexo, XML verbose
- ❌ JPA ddl-auto=update - **PERIGOSO!** Pode criar/deletar colunas sem controle
- ❌ Scripts SQL manuais - Propenso a erros, sem histórico

**Como funciona:**
```
V1__create_users_table.sql   → Executa primeiro
V2__create_tasks_table.sql   → Executa depois
V3__add_status_to_tasks.sql  → Nova coluna (futuro)
```

---

### 2.5 JWT (JSON Web Token)

**Por que:**
- ✅ Stateless (servidor não precisa guardar sessão, escala horizontalmente)
- ✅ Auto-contido (token carrega informações do usuário)
- ✅ Padrão da indústria para APIs REST
- ✅ Funciona bem com mobile e SPA (React, Angular, Vue)

**Alternativas:**
- ❌ Sessions (cookies) - Stateful, não escala bem, problema com CORS
- ❌ OAuth2 puro - Mais complexo, overkill para autenticação simples

**Como funciona:**
```
1. User faz login → Backend valida credenciais
2. Backend gera JWT assinado → Retorna para cliente
3. Cliente guarda JWT (localStorage/memory)
4. Todas as requisições → Header: Authorization: Bearer {JWT}
5. Backend valida assinatura → Permite/nega acesso
```

---

### 2.6 Lombok

**Por que:**
- ✅ Reduz boilerplate (getters, setters, constructors, equals, hashCode)
- ✅ Código mais limpo e legível
- ✅ Menos linhas de código = menos bugs

**Exemplo:**
```java
// SEM Lombok (50 linhas)
public class User {
    private Long id;
    private String username;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... 40 linhas a mais
}

// COM Lombok (5 linhas)
@Data
@Entity
public class User {
    private Long id;
    private String username;
}
```

**Alternativas:**
- ❌ Java 17 Records - Imutáveis, não funciona com JPA (@Entity precisa de setter)
- ❌ Escrever manualmente - Muito código repetitivo

---

### 2.7 Swagger/OpenAPI

**Por que:**
- ✅ Documentação automática (não desatualiza)
- ✅ Interface visual para testar API (não precisa Postman)
- ✅ Padrão da indústria (OpenAPI Specification)
- ✅ Facilita integração com frontend

**Alternativas:**
- ❌ Postman Collections - Documentação separada do código (desatualiza)
- ❌ Markdown manual - Trabalhoso e desatualiza rapidamente

---

## 3. Padrões de Projeto

### 3.1 DTO (Data Transfer Object)

**O que é:**
Objetos usados para transferir dados entre camadas (Controller ↔ Service).

**Por que:**
- ✅ **Desacoplamento** - Entity não é exposta diretamente
- ✅ **Segurança** - Não expõe campos sensíveis (ex: senha)
- ✅ **Flexibilidade** - Pode ter estrutura diferente da Entity
- ✅ **Validação** - DTO valida entrada, Entity representa banco

**Exemplo:**
```java
// Entity (banco de dados)
@Entity
public class User {
    private Long id;
    private String username;
    private String password; // 🔐 NÃO deve ser exposto
    private String email;
}

// DTO (API)
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    // 🚫 SEM password!
}
```

---

### 3.2 Service Layer Pattern

**O que é:**
Camada intermediária entre Controller e Repository, contém lógica de negócio.

**Por que:**
- ✅ **Controller magro** - Só recebe/valida/retorna dados
- ✅ **Reutilização** - Vários Controllers podem usar mesmo Service
- ✅ **Testabilidade** - Fácil mockar dependências
- ✅ **Transações** - Controle de transações no Service (@Transactional)

**Exemplo:**
```java
// ❌ RUIM: Lógica no Controller
@PostMapping
public Task create(@RequestBody TaskDTO dto) {
    Task task = new Task();
    task.setTitle(dto.getTitle());
    // ... 20 linhas de lógica
    taskRepository.save(task);
}

// ✅ BOM: Lógica no Service
@PostMapping
public Task create(@RequestBody TaskDTO dto) {
    return taskService.createTask(dto); // Controller magro!
}
```

---

### 3.3 Repository Pattern

**O que é:**
Abstração para acesso a dados (Spring Data JPA cria implementação automaticamente).

**Por que:**
- ✅ **Abstração** - Service não precisa saber SQL/JPQL
- ✅ **Testabilidade** - Fácil mockar Repository
- ✅ **Produtividade** - Spring gera queries automaticamente

**Exemplo:**
```java
// Interface (você só declara)
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId); // Spring gera SQL!
}

// Spring gera automaticamente:
// SELECT * FROM tasks WHERE user_id = ?
```

---

### 3.4 Exception Handling Global

**O que é:**
Classe centralizada que captura exceções e retorna respostas padronizadas.

**Por que:**
- ✅ **Padronização** - Todas as exceções retornam mesmo formato
- ✅ **Código limpo** - Sem try-catch em todo lugar
- ✅ **Manutenibilidade** - Uma mudança afeta toda aplicação

**Exemplo:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }
}
```

---

## 4. Segurança

### 4.1 Spring Security + JWT

**Fluxo de autenticação:**
```
1. POST /api/auth/login {username, password}
2. Backend valida credenciais
3. Se válido: Gera JWT assinado com chave secreta
4. Cliente recebe JWT e guarda (NÃO em cookies, sim em memory/localStorage)
5. Todas as próximas requisições incluem header: Authorization: Bearer {JWT}
6. Backend valida:
   - Assinatura (JWT não foi adulterado?)
   - Expiração (JWT ainda é válido?)
   - Claims (usuário existe?)
7. Se válido: Permite acesso. Se não: 401 Unauthorized
```

**Por que JWT em vez de Sessions:**
- ✅ **Stateless** - Servidor não guarda estado (escala horizontalmente)
- ✅ **Mobile-friendly** - Cookies não funcionam bem em apps nativos
- ✅ **Microservices** - Múltiplos serviços validam mesmo token

**Segurança:**
- ✅ Senha criptografada com BCrypt (hash + salt)
- ✅ JWT assinado com HS256 (HMAC + SHA-256)
- ✅ Expiration time (token expira em 24h)
- ⚠️ Sem refresh token (simplificação para POC)

---

## 5. Persistência de Dados

### 5.1 JPA + Hibernate

**Relacionamentos:**
```java
@Entity
public class User {
    @OneToMany(mappedBy = "user")
    private List<Task> tasks; // Um usuário tem várias tarefas
}

@Entity
public class Task {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Cada tarefa pertence a um usuário
}
```

**Por que JPA:**
- ✅ Abstração do SQL (mais produtivo)
- ✅ Portável (muda banco, não muda código)
- ✅ Lazy Loading (performance)
- ✅ Cache de 1º nível automático

**Quando evitar JPA:**
- ❌ Queries muito complexas → Use JPQL ou SQL nativo
- ❌ Batch updates grandes → Use JDBC direto

---

### 5.2 Estratégia de Migrations

**Convenção de nomes:**
```
V{versão}__{descrição}.sql

Exemplos:
V1__create_users_table.sql
V2__create_tasks_table.sql
V3__add_status_column_to_tasks.sql
```

**Regras:**
- ✅ Migrations são imutáveis (nunca altere um migration já aplicado)
- ✅ Sempre incremente a versão
- ✅ Rollbacks = criar novo migration que reverte mudança

---

## 6. Testes

### 6.1 Pirâmide de Testes
```
         /\
        /E2E\         ← Poucos (mais lentos, mais caros)
       /------\
      /Integr.\      ← Alguns (testam múltiplas camadas)
     /----------\
    /  Unitários \   ← Muitos (rápidos, isolados)
   /--------------\
```

**Estratégia:**
- **70% Unitários** - Service, validações, lógica de negócio
- **20% Integração** - Controller + Service + Repository + BD real
- **10% E2E** - Fluxo completo (opcional para POC)

---

### 6.2 Testes Unitários (Mockito)

**O que testamos:**
- ✅ Services (lógica de negócio)
- ✅ Validações
- ✅ Conversões (Entity ↔ DTO)

**Exemplo:**
```java
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository repository; // Repositório falso
    
    @InjectMocks
    private TaskService service; // Service real (com dependências mockadas)
    
    @Test
    void shouldCreateTask() {
        // Arrange
        TaskDTO dto = new TaskDTO("Minha tarefa");
        when(repository.save(any())).thenReturn(new Task());
        
        // Act
        Task result = service.createTask(dto);
        
        // Assert
        assertNotNull(result);
        verify(repository, times(1)).save(any()); // Verificou que salvou 1 vez
    }
}
```

---

### 6.3 Testes de Integração

**O que testamos:**
- ✅ Controller + Service + Repository + Banco real (H2 ou Testcontainers)
- ✅ Serialização JSON
- ✅ Validações de entrada

**Exemplo:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Nova tarefa\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Nova tarefa"));
    }
}
```

---

## 7. Containerização

### 7.1 Docker Compose

**Por que:**
- ✅ **Ambiente reproduzível** - "Funciona na minha máquina" não existe mais
- ✅ **Onboarding rápido** - Novo dev roda `docker-compose up` e pronto
- ✅ **CI/CD** - Mesma config em dev, staging e produção

**Serviços:**
```yaml
postgres:      # Banco de dados
  - Volume para persistir dados
  - Healthcheck para garantir que está pronto

taskmanager:   # Aplicação (futuro)
  - Depende do postgres
  - Conecta via rede interna
```

---

### 7.2 Dockerfile Multi-stage (futuro)

**Estratégia:**
```dockerfile
# Stage 1: Build
FROM maven:3.9-amazoncorretto-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
COPY --from=build target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Por que multi-stage:**
- ✅ Imagem final menor (sem Maven, só JRE)
- ✅ Build reproduzível
- ✅ Segurança (sem ferramentas de dev em produção)

---

## 📚 Referências

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [PostgreSQL Best Practices](https://wiki.postgresql.org/wiki/Don%27t_Do_This)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html)

---

**Última atualização:** Dezembro 2025