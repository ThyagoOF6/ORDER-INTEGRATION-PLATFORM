package com.orderintegration.adapter.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * DomainEventJpaEntity - JPA representation of domain events for Event Store
 *
 * Purpose:
 * - Persist domain events for complete audit trail and event replay capability
 * - Support Event Sourcing pattern for system state reconstruction
 * - Enable distributed tracing via correlationId and causationId
 * - Track event publication status for message broker delivery
 *
 * Mapping:
 * - Table: domain_events (8 columns + 6 indexes for optimal query performance)
 * - Event Store schema designed for append-only immutability
 * - JSONB columns for flexible event payload and metadata storage
 *
 * Usage Flow:
 * 1. Domain aggregate raises event (e.g., Pedido.criar() -> PedidoCriadoEvent)
 * 2. ApplicationEventPublisher captures event
 * 3. DomainEventJpaRepositoryAdapter persists to domain_events table
 * 4. Event Service publishes event to message broker (Kafka/RabbitMQ)
 * 5. Subscribers react to events via @EventListener or @KafkaListener
 *
 * @author Thyago Oliveira Ferreira
 * @version 1.0 - Phase 3 (Event Sourcing)
 * @since 2026-08-12
 */
@Entity
@Table(name = "domain_events", indexes = {
        @Index(name = "idx_domain_events_event_id", columnList = "event_id", unique = true),
        @Index(name = "idx_domain_events_aggregate_id", columnList = "aggregate_id"),
        @Index(name = "idx_domain_events_aggregate_type", columnList = "aggregate_type"),
        @Index(name = "idx_domain_events_event_type", columnList = "event_type"),
        @Index(name = "idx_domain_events_created_at", columnList = "created_at DESC"),
        @Index(name = "idx_domain_events_is_published", columnList = "is_published"),
        @Index(name = "idx_domain_events_correlation_id", columnList = "correlation_id")
})
public class DomainEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier for the event (UUID format)
     * Example: "550e8400-e29b-41d4-a716-446655440000"
     */
    @Column(name = "event_id", nullable = false, unique = true, length = 255)
    private String eventId;

    /**
     * Full qualified class name of the event
     * Example: "com.orderintegration.core.domain.order.events.PedidoCriadoEvent"
     */
    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    /**
     * ID of the aggregate that generated the event
     * Example: "pedido-123" or "550e8400-e29b-41d4-a716-446655440001"
     */
    @Column(name = "aggregate_id", nullable = false, length = 255)
    private String aggregateId;

    /**
     * Type of the aggregate (entity/class name)
     * Example: "Pedido", "Pedido", "Cliente"
     */
    @Column(name = "aggregate_type", nullable = false, length = 255)
    private String aggregateType;

    /**
     * Event data serialized as JSON
     * Contains all event attributes (e.g., codigoCliente, itens, etc.)
     * Stored as JSONB in PostgreSQL for efficient querying
     */
    @Column(name = "payload", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;

    /**
     * Additional metadata for event tracking and tracing
     * Can include: userId, ipAddress, userAgent, requestId, etc.
     */
    @Column(name = "metadata")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    /**
     * Timestamp when event was created (UTC)
     * Set by application code (onCreate/@PrePersist) if not already set
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * User or service that created the event
     * Example: "user-789", "scheduled-job", "system"
     */
    @Column(name = "created_by", length = 255)
    private String createdBy;

    /**
     * Event version (for schema evolution)
     * Default: 1
     * Incremented for backward-compatible changes
     */
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * Flag indicating if event has been published to message broker
     * Used for Event Delivery Tracking pattern
     * Query: SELECT * FROM domain_events WHERE is_published = FALSE
     * to find unpublished events for retry
     */
    @Column(name = "is_published")
    private Boolean isPublished = false;

    /**
     * Timestamp when event was published to message broker
     * Null if not yet published (is_published = FALSE)
     */
    @Column(name = "published_at")
    private Instant publishedAt;

    /**
     * Correlation ID for distributed tracing
     * Used to correlate related events across systems (SAP, message brokers, etc.)
     * Example: trace-550e8400-e29b-41d4-a716-446655440000
     */
    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    /**
     * Causation ID to link events in a cause-effect chain
     * Points to the event that caused this event
     * Example: If "PedidoValidadoEvent" caused "PedidoSincronizadoEvent",
     * then PedidoSincronizadoEvent.causationId = PedidoValidadoEvent.eventId
     */
    @Column(name = "causation_id", length = 255)
    private String causationId;

    public DomainEventJpaEntity() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCausationId() {
        return causationId;
    }

    public void setCausationId(String causationId) {
        this.causationId = causationId;
    }

    /**
     * Mark event as published to message broker
     * Called after successful publication to Kafka/RabbitMQ
     *
     * @param publishedAt Timestamp of publication
     */
    public void markAsPublished(Instant publishedAt) {
        this.isPublished = true;
        this.publishedAt = publishedAt;
    }

    /**
     * Check if event has been published
     *
     * @return true if published, false if pending
     */
    public boolean hasBeenPublished() {
        return Boolean.TRUE.equals(this.isPublished);
    }

    /**
     * Get event metadata or create empty map if null
     *
     * @return metadata map (never null)
     */
    public Map<String, Object> getMetadataOrEmpty() {
        return metadata != null ? metadata : Map.of();
    }

    /**
     * Pre-persist callback: Set createdAt if not already set
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    /**
     * String representation for logging and debugging
     */
    @Override
    public String toString() {
        return "DomainEventJpaEntity{" +
                "id=" + id +
                ", eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", aggregateId='" + aggregateId + '\'' +
                ", aggregateType='" + aggregateType + '\'' +
                ", createdAt=" + createdAt +
                ", isPublished=" + isPublished +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }

    /**
     * Manual builder (project convention avoids Lombok)
     */
    public static class Builder {
        private final DomainEventJpaEntity instance = new DomainEventJpaEntity();

        public Builder id(Long id) {
            instance.setId(id);
            return this;
        }

        public Builder eventId(String eventId) {
            instance.setEventId(eventId);
            return this;
        }

        public Builder eventType(String eventType) {
            instance.setEventType(eventType);
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            instance.setAggregateId(aggregateId);
            return this;
        }

        public Builder aggregateType(String aggregateType) {
            instance.setAggregateType(aggregateType);
            return this;
        }

        public Builder payload(Map<String, Object> payload) {
            instance.setPayload(payload);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            instance.setMetadata(metadata);
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            instance.setCreatedAt(createdAt);
            return this;
        }

        public Builder createdBy(String createdBy) {
            instance.setCreatedBy(createdBy);
            return this;
        }

        public Builder version(Integer version) {
            instance.setVersion(version);
            return this;
        }

        public Builder isPublished(Boolean isPublished) {
            instance.setIsPublished(isPublished);
            return this;
        }

        public Builder publishedAt(Instant publishedAt) {
            instance.setPublishedAt(publishedAt);
            return this;
        }

        public Builder correlationId(String correlationId) {
            instance.setCorrelationId(correlationId);
            return this;
        }

        public Builder causationId(String causationId) {
            instance.setCausationId(causationId);
            return this;
        }

        public DomainEventJpaEntity build() {
            return instance;
        }
    }
}
