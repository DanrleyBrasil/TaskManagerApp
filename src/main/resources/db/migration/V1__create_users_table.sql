-- ============================================
-- MIGRATION V1: Criar tabela de usuários
-- Data: Dezembro 2025
-- Descrição: Tabela para armazenar usuários do sistema
-- ============================================

CREATE TABLE users (
    -- ID auto-incrementado (chave primária)
    -- BIGSERIAL = tipo PostgreSQL que cria sequência automática
                       id BIGSERIAL PRIMARY KEY,

    -- Nome de usuário (login)
    -- VARCHAR(50) = até 50 caracteres
    -- NOT NULL = obrigatório
    -- UNIQUE = não pode ter usuário duplicado
                       username VARCHAR(50) NOT NULL UNIQUE,

    -- Email do usuário
    -- VARCHAR(100) = até 100 caracteres
    -- NOT NULL = obrigatório
    -- UNIQUE = não pode ter email duplicado
                       email VARCHAR(100) NOT NULL UNIQUE,

    -- Senha criptografada (BCrypt gera hash de 60 caracteres)
    -- VARCHAR(255) = espaço suficiente para hash + salt
    -- NOT NULL = obrigatório
                       password VARCHAR(255) NOT NULL,

    -- Data de criação do usuário
    -- TIMESTAMP = data e hora completa
    -- DEFAULT CURRENT_TIMESTAMP = preenche automaticamente com data/hora atual
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Data da última atualização
    -- NULL por padrão (será preenchido quando houver update)
                       updated_at TIMESTAMP
);

-- ============================================
-- ÍNDICES (para melhorar performance de busca)
-- ============================================

-- Índice para buscar por username (usado no login)
-- B-tree é o tipo padrão e mais eficiente para buscas exatas
CREATE INDEX idx_users_username ON users(username);

-- Índice para buscar por email (usado no login alternativo)
CREATE INDEX idx_users_email ON users(email);

-- ============================================
-- COMENTÁRIOS (documentação no banco)
-- ============================================

-- Documenta a tabela
COMMENT ON TABLE users IS 'Armazena usuários do sistema Task Manager';

-- Documenta cada coluna
COMMENT ON COLUMN users.id IS 'Identificador único do usuário';
COMMENT ON COLUMN users.username IS 'Nome de usuário para login (único)';
COMMENT ON COLUMN users.email IS 'Email do usuário (único)';
COMMENT ON COLUMN users.password IS 'Senha criptografada com BCrypt';
COMMENT ON COLUMN users.created_at IS 'Data e hora de criação do registro';
COMMENT ON COLUMN users.updated_at IS 'Data e hora da última atualização';