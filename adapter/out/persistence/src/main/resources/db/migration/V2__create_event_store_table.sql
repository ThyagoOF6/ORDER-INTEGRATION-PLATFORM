-- Flyway Migration V2: Create Event Store Table for Event Sourcing
-- Purpose: Persist all domain events for complete audit trail and event replay
-- Created: 2026-08-12
-- Phase: 3 (Event Sourcing & Message Broker Integration)

CREATE TABLE IF NOT EXISTS domain_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 1,
    is_published BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP WITH TIME ZONE,
    correlation_id VARCHAR(255),
    causation_id VARCHAR(255)
);

-- Índices para otimização de queries
CREATE INDEX idx_domain_events_event_id ON domain_events(event_id);
CREATE INDEX idx_domain_events_aggregate_id ON domain_events(aggregate_id);
CREATE INDEX idx_domain_events_aggregate_type ON domain_events(aggregate_type);
CREATE INDEX idx_domain_events_event_type ON domain_events(event_type);
CREATE INDEX idx_domain_events_created_at ON domain_events(created_at DESC);
CREATE INDEX idx_domain_events_is_published ON domain_events(is_published);
CREATE INDEX idx_domain_events_correlation_id ON domain_events(correlation_id);

-- JSONB GIN index para efficient JSONB queries
CREATE INDEX idx_domain_events_payload_gin ON domain_events USING GIN(payload);

-- Comment para documentação
COMMENT ON TABLE domain_events IS 'Event Store: Persists all domain events for event sourcing, audit trail, and event replay';
COMMENT ON COLUMN domain_events.event_id IS 'Unique identifier for the event (UUID format)';
COMMENT ON COLUMN domain_events.event_type IS 'Full qualified class name of the event (e.g., com.orderintegration.core.domain.order.events.PedidoCriadoEvent)';
COMMENT ON COLUMN domain_events.aggregate_id IS 'ID of the aggregate that generated the event (e.g., pedido-123)';
COMMENT ON COLUMN domain_events.aggregate_type IS 'Type of the aggregate (e.g., Pedido)';
COMMENT ON COLUMN domain_events.payload IS 'Event data serialized as JSON (contains all event attributes)';
COMMENT ON COLUMN domain_events.metadata IS 'Additional metadata (correlationId, causationId, userId, etc.)';
COMMENT ON COLUMN domain_events.is_published IS 'Flag indicating if event has been published to message broker';
COMMENT ON COLUMN domain_events.correlation_id IS 'For distributed tracing: correlates related events across systems';
COMMENT ON COLUMN domain_events.causation_id IS 'For event causation: links to the event that caused this event';
