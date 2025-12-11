# 🎯 Task Manager API

API RESTful para gerenciamento de tarefas com autenticação JWT, construída com Spring Boot 4.0.0 e Java 21.

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Como Executar](#como-executar)
- [Endpoints da API](#endpoints-da-api)
- [Testes](#testes)
- [Documentação](#documentação)

---

## 🎓 Sobre o Projeto

Este projeto foi criado como **Proof of Concept (POC)** para praticar conceitos essenciais de desenvolvimento back-end:

- ✅ API REST completa (CRUD)
- ✅ Autenticação e autorização com JWT
- ✅ Persistência de dados com JPA/Hibernate
- ✅ Versionamento de banco com Flyway
- ✅ Containerização com Docker
- ✅ Testes unitários e de integração
- ✅ CI/CD com GitHub Actions
- ✅ Documentação automática com Swagger

**Caso de uso:** Sistema onde usuários podem criar, listar, atualizar e deletar suas próprias tarefas após autenticação.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão           | Finalidade |
|-----------|------------------|-----------|
| Java | 21               | Linguagem de programação |
| Spring Boot | 4.0.0            | Framework para aplicações Java |
| Spring Data JPA | 4.0.0            | Abstração para acesso a dados |
| Spring Security | 4.0.0            | Autenticação e autorização |
| PostgreSQL | 16               | Banco de dados relacional |
| Flyway | 11.14.1          | Migrations e versionamento de BD |
| JWT (jjwt) | 0.13.0           | Geração e validação de tokens |
| Lombok | 1.18.42          | Redução de boilerplate |
| Springdoc OpenAPI | 3.0.0            | Documentação Swagger/OpenAPI |
| JUnit 5 | 6.0.1            | Framework de testes |
| Mockito | 5.20.0           | Mocks para testes unitários |
| Docker | 28.5.1           | Containerização |
| Docker Compose | 2.40.2-desktop.1 | Orquestração de containers |
| Maven | --               | Gerenciamento de dependências |

---

## 🏗️ Arquitetura

O projeto segue a **Arquitetura em Camadas (Layered Architecture)**, separando responsabilidades:
```
┌─────────────────────────────────────────┐
│         CLIENTE (Frontend/Postman)       │
└──────────────────┬──────────────────────┘
                   │ HTTP Request
                   ▼
┌─────────────────────────────────────────┐
│       CAMADA DE APRESENTAÇÃO            │
│    (Controllers + Exception Handlers)   │
│  - Recebe requisições HTTP              │
│  - Valida entrada                       │
│  - Retorna respostas HTTP               │
└──────────────────┬──────────────────────┘
                   │ DTOs
                   ▼
┌─────────────────────────────────────────┐
│       CAMADA DE NEGÓCIO                 │
│            (Services)                   │
│  - Lógica de negócio                    │
│  - Validações complexas                 │
│  - Orquestração de operações            │
└──────────────────┬──────────────────────┘
                   │ Entities
                   ▼
┌─────────────────────────────────────────┐
│       CAMADA DE PERSISTÊNCIA            │
│          (Repositories)                 │
│  - Acesso ao banco de dados             │
│  - Queries SQL (JPA/JPQL)               │
└──────────────────┬──────────────────────┘
                   │ SQL
                   ▼
┌─────────────────────────────────────────┐
│          BANCO DE DADOS                 │
│           (PostgreSQL)                  │
└─────────────────────────────────────────┘
```

**Por que essa arquitetura?**
- ✅ **Separação de responsabilidades** (cada camada tem um propósito claro)
- ✅ **Facilita testes** (podemos testar cada camada isoladamente)
- ✅ **Manutenibilidade** (mudanças em uma camada não afetam as outras)
- ✅ **Reutilização** (Services podem ser usados por múltiplos Controllers)

📖 **Mais detalhes:** Veja [ARCHITECTURE.md](./ARCHITECTURE.md)

---

## 📁 Estrutura do Projeto
```
task-manager/
├── src/
│   ├── main/
│   │   ├── java/com/taskmanager/
│   │   │   ├── config/              # Configurações (Security, Swagger, JWT)
│   │   │   ├── controller/          # Endpoints REST
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── exception/           # Tratamento de exceções
│   │   │   ├── model/               # Entidades JPA
│   │   │   ├── repository/          # Repositórios (acesso a dados)
│   │   │   ├── service/             # Lógica de negócio
│   │   │   └── TaskManagerApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Scripts Flyway
│   └── test/                        # Testes unitários e integração
├── docker-compose.yml               # Configuração Docker
├── Dockerfile                       # (a criar) Imagem da aplicação
├── pom.xml                          # Dependências Maven
├── README.md                        # Este arquivo
└── ARCHITECTURE.md                  # Decisões arquiteturais
```

---

## ⚙️ Pré-requisitos

Antes de executar o projeto, você precisa ter instalado:

- **Java 21** - [Download](https://adoptium.net/)
- **Docker** - [Download](https://www.docker.com/get-started)
- **Docker Compose** - [Download](https://docs.docker.com/compose/install/)
- **Git** - [Download](https://git-scm.com/)

Verifique as instalações:
```bash
java -version    # Deve mostrar Java 21
mvn -version     # Deve mostrar Maven 3.9+
docker --version
docker-compose --version
```

---

## 🚀 Como Executar

### 1️⃣ Clone o repositório
```bash
git clone https://github.com/DanrleyBrasil/task-manager.git
cd task-manager
```

### 2️⃣ Suba o banco de dados (PostgreSQL)
```bash
docker-compose up -d
```

Verifique se está rodando:
```bash
docker-compose ps
```

### 3️⃣ Execute a aplicação
```bash
mvn spring-boot:run
```

A aplicação estará disponível em: **http://localhost:8080**

### 4️⃣ Acesse a documentação Swagger
Abra no navegador: **http://localhost:8080/swagger-ui.html**

---

## 📡 Endpoints da API

### 🔐 Autenticação (Público)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/register` | Registrar novo usuário |
| POST | `/api/auth/login` | Login (retorna JWT) |

### ✅ Tarefas (Requer autenticação JWT)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/tasks` | Listar todas as tarefas do usuário |
| GET | `/api/tasks/{id}` | Buscar tarefa por ID |
| POST | `/api/tasks` | Criar nova tarefa |
| PUT | `/api/tasks/{id}` | Atualizar tarefa |
| DELETE | `/api/tasks/{id}` | Deletar tarefa |

**Autenticação:** Adicione o header `Authorization: Bearer {seu-token-jwt}`

---

## 🧪 Testes

### Executar todos os testes
```bash
mvn test
```

### Executar com cobertura
```bash
mvn test jacoco:report
```

O relatório de cobertura estará em: `target/site/jacoco/index.html`

---

## 📚 Documentação

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs
- **Decisões Arquiteturais:** [ARCHITECTURE.md](./ARCHITECTURE.md)

---

## 🤝 Contribuindo

Este é um projeto de estudo, mas sugestões são bem-vindas!

1. Fork o projeto
2. Crie uma branch (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Add: nova feature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

---

## 📝 Licença

Este projeto é de código aberto e está sob a licença MIT.

---

## 👤 Autor

**Danrley** - [LinkedIn](https://www.linkedin.com/in/danrleybrasil/) | [GitHub](https://github.com/DanrleyBrasil)

---

## 🎯 Próximos Passos (Roadmap)

- [ ] Construção das classes necessárias
- [ ] Adicionar paginação nos endpoints
- [ ] Implementar filtros de busca
- [ ] Adicionar testes de performance
- [ ] Configurar CI/CD com GitHub Actions
- [ ] Deploy na AWS
- [ ] Integração com mensageria (RabbitMQ/Kafka)