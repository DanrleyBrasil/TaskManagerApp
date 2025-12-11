-- ============================================
-- MIGRATION V3: Sistema de Roles com tabelas separadas
-- Data: Dezembro 2025
-- Descrição: Implementa RBAC (Role-Based Access Control) com relacionamento muitos-para-muitos
-- ============================================

-- ============================================
-- TABELA: roles
-- ============================================
-- Armazena os perfis/roles disponíveis no sistema
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,

    -- Nome do role (USER, ADMIN, etc)
    -- VARCHAR(50) para flexibilidade futura
    -- UNIQUE garante que não teremos role duplicado
                       name VARCHAR(50) NOT NULL UNIQUE,

    -- Descrição do que esse role pode fazer (opcional, mas útil)
                       description VARCHAR(255),

                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- TABELA: user_roles (relacionamento N:N)
-- ============================================
-- Tabela de junção entre users e roles
-- Um usuário pode ter múltiplos roles
-- Um role pode pertencer a múltiplos usuários
CREATE TABLE user_roles (
    -- Chave estrangeira para users
                            user_id BIGINT NOT NULL,

    -- Chave estrangeira para roles
                            role_id BIGINT NOT NULL,

    -- Chave primária composta (garante que não teremos duplicatas)
    -- Um usuário não pode ter o mesmo role duas vezes
                            PRIMARY KEY (user_id, role_id),

    -- Constraint: FK para users
    -- ON DELETE CASCADE: Se usuário for deletado, remove seus roles
                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE,

    -- Constraint: FK para roles
    -- ON DELETE CASCADE: Se role for deletado, remove das associações
                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
                                    ON DELETE CASCADE
);

-- ============================================
-- ÍNDICES (Performance)
-- ============================================

-- Índice para buscar roles de um usuário
-- Query: SELECT * FROM user_roles WHERE user_id = ?
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- Índice para buscar usuários de um role
-- Query: SELECT * FROM user_roles WHERE role_id = ?
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Índice no nome do role (buscas frequentes)
CREATE INDEX idx_roles_name ON roles(name);

-- ============================================
-- SEED DATA: Inserir roles padrão
-- ============================================

-- Role: USER (usuário comum)
INSERT INTO roles (name, description)
VALUES ('ROLE_USER', 'Usuário comum - pode gerenciar suas próprias tarefas');

-- Role: ADMIN (administrador)
INSERT INTO roles (name, description)
VALUES ('ROLE_ADMIN', 'Administrador - acesso total ao sistema');

-- ============================================
-- CRIAR USUÁRIO ADMIN PADRÃO
-- ============================================

-- Inserir usuário admin (se não existir)
INSERT INTO users (username, email, password, created_at)
VALUES (
           'admin',
           'admin@taskmanager.com',
           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- "admin123"
           CURRENT_TIMESTAMP
       )
    ON CONFLICT (username) DO NOTHING;

-- Associar role ADMIN ao usuário admin
-- Primeiro busca o ID do usuário e do role, depois associa
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin'
  AND r.name = 'ROLE_ADMIN'
    ON CONFLICT DO NOTHING;

-- ============================================
-- COMENTÁRIOS (Documentação)
-- ============================================

COMMENT ON TABLE roles IS 'Armazena os perfis/roles do sistema (USER, ADMIN, etc)';
COMMENT ON TABLE user_roles IS 'Relacionamento N:N entre usuários e roles';

COMMENT ON COLUMN roles.name IS 'Nome do role (ex: ROLE_USER, ROLE_ADMIN)';
COMMENT ON COLUMN roles.description IS 'Descrição das permissões do role';

COMMENT ON COLUMN user_roles.user_id IS 'ID do usuário (FK)';
COMMENT ON COLUMN user_roles.role_id IS 'ID do role (FK)';