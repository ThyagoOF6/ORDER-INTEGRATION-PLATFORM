package com.orderintegration.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * DomainEventDto - Data Transfer Object for domain events in Event Store
 *
 * Purpose:
 * - DTO for Event Store operations (not JPA entity)
 * - Bridge between application layer (port) and infrastructure (adapter)
 * - Serializable to JSON for message brokers (Kafka, RabbitMQ, Azure Bus)
 *
 * Architecture:
 * - Application layer: Works with DTO (clean boundaries)
 * - Adapter layer: Maps DTO <-> JPA Entity
 * - Message brokers: Serialize/deserialize DTO to JSON
 *
 * @author Thyago Oliveira Ferreira
 * @version 1.0 - Phase 3 (Event Sourcing)
 * @since 2026-08-12
 */
public class DomainEventDto {

    /**
     * Unique identifier for the event (UUID format)
     * Example: "550e8400-e29b-41d4-a716-446655440000"
     */
    @JsonProperty("eventId")
    private String eventId;

    /**
     * Full qualified class name of the event
     * Example: "com.orderintegration.core.domain.order.events.PedidoCriadoEvent"
     */
    @JsonProperty("eventType")
    private String eventType;

    /**
     * ID of the aggregate that generated the event
     * Example: "pedido-123"
     */
    @JsonProperty("aggregateId")
    private String aggregateId;

    /**
     * Type of the aggregate
     * Example: "Pedido"
     */
    @JsonProperty("aggregateType")
    private String aggregateType;

    /**
     * Event data serialized as JSON
     * Contains all event attributes
     */
    @JsonProperty("payload")
    private Map<String, Object> payload;

    /**
     * Additional metadata for event tracking
     */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * Timestamp when event was created (UTC)
     */
    @JsonProperty("createdAt")
    private Instant createdAt;

    /**
     * User or service that created the event
     */
    @JsonProperty("createdBy")
    private String createdBy;

    /**
     * Event version (for schema evolution)
     */
    @JsonProperty("version")
    private Integer version = 1;

    /**
     * Flag indicating if event has been published to message broker
     */
    @JsonProperty("isPublished")
    private Boolean isPublished = false;

    /**
     * Timestamp when event was published
     */
    @JsonProperty("publishedAt")
    private Instant publishedAt;

    /**
     * Correlation ID for distributed tracing
     */
    @JsonProperty("correlationId")
    private String correlationId;

    /**
     * Causation ID for event linking
     */
    @JsonProperty("causationId")
    private String causationId;

    public DomainEventDto() {
    }

    public static Builder builder() {
        return new Builder();
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
     * Check if event has been published
     *
     * @return true if published
     */
    public boolean hasBeenPublished() {
        return Boolean.TRUE.equals(this.isPublished);
    }

    /**
     * Get metadata or empty map if null
     *
     * @return metadata map (never null)
     */
    public Map<String, Object> getMetadataOrEmpty() {
        return metadata != null ? metadata : Map.of();
    }

    /**
     * Manual builder (project convention avoids Lombok)
     */
    public static class Builder {
        private final DomainEventDto instance = new DomainEventDto();

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

        public DomainEventDto build() {
            return instance;
        }
    }
}
