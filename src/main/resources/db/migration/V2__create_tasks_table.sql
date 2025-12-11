-- ============================================
-- MIGRATION V2: Criar tabela de tarefas
-- Data: Dezembro 2025
-- Descrição: Tabela para armazenar tarefas dos usuários
-- ============================================

CREATE TABLE tasks (
    -- ID auto-incrementado (chave primária)
                       id BIGSERIAL PRIMARY KEY,

    -- Título da tarefa
    -- VARCHAR(200) = até 200 caracteres
    -- NOT NULL = obrigatório
                       title VARCHAR(200) NOT NULL,

    -- Descrição detalhada da tarefa
    -- TEXT = tamanho ilimitado (para descrições longas)
    -- NULL por padrão (descrição é opcional)
                       description TEXT,

    -- Status da tarefa
    -- VARCHAR(20) = valores fixos (PENDING, IN_PROGRESS, COMPLETED)
    -- DEFAULT 'PENDING' = toda tarefa começa como pendente
                       status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    -- Chave estrangeira (FK) para a tabela users
    -- Indica qual usuário é "dono" desta tarefa
    -- NOT NULL = toda tarefa DEVE ter um usuário
                       user_id BIGINT NOT NULL,

    -- Data de criação da tarefa
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Data da última atualização
                       updated_at TIMESTAMP,

    -- ============================================
    -- CONSTRAINT: Chave Estrangeira (Foreign Key)
    -- ============================================
    -- Define relacionamento entre tasks e users
    -- ON DELETE CASCADE = se usuário for deletado, suas tarefas também são deletadas
    -- ON UPDATE CASCADE = se user_id mudar (raro), atualiza automaticamente aqui
                       CONSTRAINT fk_tasks_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE
                               ON UPDATE CASCADE,

    -- ============================================
    -- CONSTRAINT: Check (validação no banco)
    -- ============================================
    -- Garante que status só pode ter valores válidos
    -- Se tentar inserir outro valor, banco rejeita
                       CONSTRAINT chk_tasks_status
                           CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED'))
);

-- ============================================
-- ÍNDICES (performance)
-- ============================================

-- Índice para buscar tarefas de um usuário específico
-- Consulta: SELECT * FROM tasks WHERE user_id = ?
-- Muito usado! Usuário sempre quer ver SUAS tarefas
CREATE INDEX idx_tasks_user_id ON tasks(user_id);

-- Índice composto para buscar tarefas de um usuário por status
-- Consulta: SELECT * FROM tasks WHERE user_id = ? AND status = ?
-- Exemplo: "Mostrar minhas tarefas PENDING"
CREATE INDEX idx_tasks_user_status ON tasks(user_id, status);

-- Índice para buscar tarefas por data de criação
-- Útil para ordenar por mais recente
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);

-- ============================================
-- COMENTÁRIOS
-- ============================================

COMMENT ON TABLE tasks IS 'Armazena tarefas dos usuários';
COMMENT ON COLUMN tasks.id IS 'Identificador único da tarefa';
COMMENT ON COLUMN tasks.title IS 'Título da tarefa (obrigatório)';
COMMENT ON COLUMN tasks.description IS 'Descrição detalhada (opcional)';
COMMENT ON COLUMN tasks.status IS 'Status: PENDING, IN_PROGRESS, COMPLETED';
COMMENT ON COLUMN tasks.user_id IS 'ID do usuário dono da tarefa (FK)';
COMMENT ON COLUMN tasks.created_at IS 'Data e hora de criação';
COMMENT ON COLUMN tasks.updated_at IS 'Data e hora da última atualização';