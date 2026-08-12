-- V1__create_pedido_tables.sql
-- Migração Flyway para criação das tabelas de Pedido

-- Tabela de Pedidos
CREATE TABLE pedido (
    pedido_id VARCHAR(36) PRIMARY KEY,
    codigo_cliente VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    valor_total NUMERIC(12, 2) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    mensagem_erro VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pedido_cliente ON pedido(codigo_cliente);
CREATE INDEX idx_pedido_status ON pedido(status);
CREATE INDEX idx_pedido_criado_em ON pedido(criado_em);

-- Tabela de Itens de Pedido
CREATE TABLE item_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id VARCHAR(36) NOT NULL REFERENCES pedido(pedido_id) ON DELETE CASCADE,
    codigo_produto VARCHAR(50) NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_item_pedido_pedido_id ON item_pedido(pedido_id);
CREATE INDEX idx_item_pedido_produto ON item_pedido(codigo_produto);

-- Comentários das tabelas
COMMENT ON TABLE pedido IS 'Agregado raiz de Pedidos';
COMMENT ON COLUMN pedido.pedido_id IS 'ID único do pedido em UUID';
COMMENT ON COLUMN pedido.codigo_cliente IS 'Código identificador do cliente';
COMMENT ON COLUMN pedido.status IS 'Status do pedido: CRIADO, VALIDADO, SINCRONIZANDO, SINCRONIZADO, ERRO';
COMMENT ON COLUMN pedido.valor_total IS 'Valor total do pedido em reais';
COMMENT ON COLUMN pedido.criado_em IS 'Data/hora de criação do pedido';
COMMENT ON COLUMN pedido.mensagem_erro IS 'Mensagem de erro em caso de sincronização falha';

COMMENT ON TABLE item_pedido IS 'Itens que compõem um pedido';
COMMENT ON COLUMN item_pedido.codigo_produto IS 'Código do produto no catálogo';
COMMENT ON COLUMN item_pedido.quantidade IS 'Quantidade de unidades';
COMMENT ON COLUMN item_pedido.preco_unitario IS 'Preço unitário do produto';
