package com.orderintegration.application.port;

import com.orderintegration.application.dto.DomainEventDto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * DomainEventRepositoryPort - Hexagonal port for Event Store persistence
 *
 * Purpose:
 * - Define contract for persisting and querying domain events
 * - Abstract away database implementation details
 * - Support Event Sourcing pattern: store immutable events, replay history
 * - Track event publication status for message broker delivery guarantee
 *
 * Architecture Pattern:
 * - Hexagonal Architecture: Port defines contract, Adapter (JPA) implements
 * - Clean Dependency: Application layer depends on port, not on database
 * - SOLID Principles: Single Responsibility (events), Dependency Inversion
 *
 * Usage:
 * 1. After domain event raised: persistEvent(event) -> store in domain_events
 * table
 * 2. Find unpublished events: findUnpublishedEvents() -> retry failed
 * publications
 * 3. Get event history: getEventsByAggregateId() -> reconstruct aggregate state
 * 4. Track correlations: events linked via correlationId for distributed
 * tracing
 *
 * @author Thyago Oliveira Ferreira
 * @version 1.0 - Phase 3 (Event Sourcing)
 * @since 2026-08-12
 */
public interface DomainEventRepositoryPort {

    /**
     * Persist a domain event to the Event Store
     *
     * Contract:
     * - Event must have unique eventId (enforced by database constraint)
     * - Event persists immutably (no updates after creation)
     * - Transaction ensures atomic write
     * - Returns persisted entity with database-assigned ID
     *
     * Example:
     * DomainEventJpaEntity event = DomainEventJpaEntity.builder()
     * .eventId(UUID.randomUUID().toString())
     * .eventType("com.orderintegration.core.domain.order.events.PedidoCriadoEvent")
     * .aggregateId("pedido-123")
     * .aggregateType("Pedido")
     * .payload(Map.of("codigoCliente", "CLI-001", "itens", [...]))
     * .correlationId(trace-id)
     * .build();
     * persistEvent(event);
     *
     * @param event DomainEventDto to persist
     * @return Persisted entity (with database-assigned ID)
     * @throws EventPersistenceException if persistence fails
     */
    DomainEventDto persistEvent(DomainEventDto event);

    /**
     * Find event by unique event ID
     *
     * Contract:
     * - Event ID is unique across entire event store
     * - Returns Optional.empty() if not found
     * - Used for duplicate detection, idempotent event processing
     *
     * @param eventId Unique event identifier (UUID format)
     * @return Optional containing event if found
     */
    Optional<DomainEventDto> findByEventId(String eventId);

    /**
     * Get all events for a specific aggregate (replay history)
     *
     * Contract:
     * - Returns events in chronological order (created_at ASC)
     * - Used for aggregate reconstruction via event replay
     * - Critical for Event Sourcing: rebuild Pedido state from events
     *
     * Example:
     * List<DomainEventJpaEntity> events = getEventsByAggregateId("pedido-123");
     * // Replay: PedidoCriadoEvent -> PedidoValidadoEvent ->
     * PedidoSincronizadoEvent
     * // Result: Pedido object with final state
     *
     * @param aggregateId ID of aggregate (e.g., "pedido-123")
     * @return List of events in chronological order (empty list if no events)
     */
    List<DomainEventDto> getEventsByAggregateId(String aggregateId);

    /**
     * Get all events of a specific type across all aggregates
     *
     * Contract:
     * - Returns events by event type (e.g., "PedidoCriadoEvent")
     * - Useful for reporting, analytics, testing
     * - Can be used to trigger subscribers
     *
     * @param eventType Full qualified event class name
     * @return List of events of this type
     */
    List<DomainEventDto> getEventsByType(String eventType);

    /**
     * Find all unpublished events (awaiting message broker delivery)
     *
     * Contract:
     * - Returns events where is_published = FALSE
     * - Used for "Event Delivery Tracking" pattern
     * - Critical for ensuring no events are lost (durability guarantee)
     * - Application will retry publishing until successful
     *
     * Example:
     * List<DomainEventJpaEntity> unpublished = findUnpublishedEvents();
     * for (event : unpublished) {
     * try {
     * kafkaTemplate.send(event);
     * markAsPublished(event.getEventId());
     * } catch (Exception e) {
     * // Retry on next cycle
     * }
     * }
     *
     * @return List of unpublished events (can be empty)
     */
    List<DomainEventDto> findUnpublishedEvents();

    /**
     * Get events published within a time range (for analytics/reporting)
     *
     * Contract:
     * - Returns only published events (is_published = TRUE)
     * - Filtered by publishedAt timestamp
     * - Useful for dashboards, audit reports
     *
     * @param startTime Start of time range (UTC)
     * @param endTime   End of time range (UTC)
     * @return List of published events in time range
     */
    List<DomainEventDto> getPublishedEventsBetween(Instant startTime, Instant endTime);

    /**
     * Find events by correlation ID (distributed tracing)
     *
     * Contract:
     * - Returns all events linked by correlation ID
     * - Used to trace a request across multiple systems
     * - Example: Request enters system -> generates correlationId -> all downstream
     * events share it
     *
     * Usage:
     * List<DomainEventJpaEntity> relatedEvents =
     * getEventsByCorrelationId("trace-xyz");
     * // Shows complete event flow for debugging, troubleshooting
     *
     * @param correlationId Correlation ID for distributed tracing
     * @return List of events with this correlation ID
     */
    List<DomainEventDto> getEventsByCorrelationId(String correlationId);

    /**
     * Mark an event as published to message broker
     *
     * Contract:
     * - Updates is_published = TRUE and published_at = NOW()
     * - Atomic operation (single SQL update)
     * - Called after successful publication to Kafka/RabbitMQ
     *
     * @param eventId Event ID to mark as published
     * @throws EventNotFoundException if event doesn't exist
     */
    void markAsPublished(String eventId);

    /**
     * Mark an event as published with explicit timestamp
     *
     * @param eventId     Event ID to mark
     * @param publishedAt Explicit publication timestamp
     * @throws EventNotFoundException if event doesn't exist
     */
    void markAsPublished(String eventId, Instant publishedAt);

    /**
     * Delete an event (careful: should rarely be used in Event Sourcing)
     *
     * Contract:
     * - Removes event from Event Store permanently
     * - Use only for GDPR right-to-be-forgotten or data correction
     * - Not recommended for normal event processing
     * - Consider archiving to separate table instead of deleting
     *
     * @param eventId Event ID to delete
     * @return true if deleted, false if not found
     */
    boolean deleteEvent(String eventId);

    /**
     * Count total events in Event Store
     *
     * @return Total number of events persisted
     */
    long countAllEvents();

    /**
     * Count events for a specific aggregate
     *
     * @param aggregateId Aggregate ID
     * @return Number of events for this aggregate
     */
    long countEventsByAggregateId(String aggregateId);

    /**
     * Exception for Event Store operations
     */
    class EventStoreException extends RuntimeException {
        public EventStoreException(String message) {
            super(message);
        }

        public EventStoreException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception when event is not found
     */
    class EventNotFoundException extends EventStoreException {
        public EventNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Exception when event persistence fails
     */
    class EventPersistenceException extends EventStoreException {
        public EventPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
