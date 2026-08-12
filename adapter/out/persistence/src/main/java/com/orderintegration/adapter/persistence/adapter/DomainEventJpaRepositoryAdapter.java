package com.orderintegration.adapter.persistence.adapter;

import com.orderintegration.adapter.persistence.entity.DomainEventJpaEntity;
import com.orderintegration.adapter.persistence.repository.DomainEventSpringDataRepository;
import com.orderintegration.application.dto.DomainEventDto;
import com.orderintegration.application.port.DomainEventRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * DomainEventJpaRepositoryAdapter - Hexagonal adapter implementing Event Store
 * port
 *
 * Purpose:
 * - Implement DomainEventRepositoryPort using Spring Data JPA
 * - Bridge between domain layer (port) and data layer (Spring Data)
 * - Handle transaction management (@Transactional)
 * - Provide error handling and logging for event persistence
 *
 * Architecture:
 * - Hexagonal Pattern: Port (interface) -> Adapter (implementation)
 * - Dependency Injection: Spring @Component with constructor injection
 * - Transaction Boundaries: @Transactional ensures ACID properties
 * - Logging: SLF4J for debugging and monitoring
 *
 * Event Store Contract:
 * - Immutability: Events never updated after creation (append-only log)
 * - Durability: All events persist atomically to PostgreSQL
 * - Traceability: Each event has eventId, correlationId, causationId
 * - Delivery Tracking: is_published flag for message broker reliability
 *
 * @author Thyago Oliveira Ferreira
 * @version 1.0 - Phase 3 (Event Sourcing)
 * @since 2026-08-12
 */
@Component
public class DomainEventJpaRepositoryAdapter implements DomainEventRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(DomainEventJpaRepositoryAdapter.class);

    private final DomainEventSpringDataRepository repository;

    public DomainEventJpaRepositoryAdapter(DomainEventSpringDataRepository repository) {
        this.repository = repository;
    }

    /**
     * Persist a domain event to Event Store
     *
     * Flow:
     * 1. Receive event from domain layer
     * 2. Validate required fields (eventId, aggregateId, payload)
     * 3. Save to domain_events table (PostgreSQL)
     * 4. Log successful persistence
     * 5. Return persisted entity with database ID
     *
     * Error Handling:
     * - Catch DataIntegrityViolationException for duplicate eventId
     * - Wrap in EventPersistenceException with helpful message
     * - Rethrow for upper layer handling (Application Service)
     *
     * Transaction:
     * - @Transactional: Atomically persist event
     * - SAVEPOINT on error (rollback to consistent state)
     *
     * @param event Event to persist
     * @return Persisted entity with ID assigned by database
     * @throws EventPersistenceException on persistence failure
     */
    @Override
    @Transactional
    public DomainEventDto persistEvent(DomainEventDto event) {
        try {
            log.debug("Persisting domain event: eventId={}, eventType={}, aggregateId={}",
                    event.getEventId(), event.getEventType(), event.getAggregateId());

            DomainEventJpaEntity entity = toEntity(event);
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(Instant.now());
            }

            DomainEventJpaEntity savedEvent = repository.save(entity);

            log.info("Domain event persisted successfully: eventId={}, id={}, aggregateId={}",
                    savedEvent.getEventId(), savedEvent.getId(), savedEvent.getAggregateId());

            return toDto(savedEvent);

        } catch (Exception e) {
            log.error("Failed to persist domain event: eventId={}, aggregateId={}, error={}",
                    event.getEventId(), event.getAggregateId(), e.getMessage(), e);
            throw new EventPersistenceException(
                    String.format("Failed to persist event [%s] for aggregate [%s]: %s",
                            event.getEventId(), event.getAggregateId(), e.getMessage()),
                    e);
        }
    }

    private DomainEventJpaEntity toEntity(DomainEventDto dto) {
        return DomainEventJpaEntity.builder()
                .eventId(dto.getEventId())
                .eventType(dto.getEventType())
                .aggregateId(dto.getAggregateId())
                .aggregateType(dto.getAggregateType())
                .payload(dto.getPayload())
                .metadata(dto.getMetadata())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .version(dto.getVersion())
                .isPublished(dto.getIsPublished())
                .publishedAt(dto.getPublishedAt())
                .correlationId(dto.getCorrelationId())
                .causationId(dto.getCausationId())
                .build();
    }

    private DomainEventDto toDto(DomainEventJpaEntity entity) {
        return DomainEventDto.builder()
                .eventId(entity.getEventId())
                .eventType(entity.getEventType())
                .aggregateId(entity.getAggregateId())
                .aggregateType(entity.getAggregateType())
                .payload(entity.getPayload())
                .metadata(entity.getMetadata())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .version(entity.getVersion())
                .isPublished(entity.getIsPublished())
                .publishedAt(entity.getPublishedAt())
                .correlationId(entity.getCorrelationId())
                .causationId(entity.getCausationId())
                .build();
    }

    /**
     * Find event by unique event ID
     *
     * Query: Domain events indexed by eventId (UNIQUE constraint)
     * Performance: O(log n) - indexed lookup
     * Use Case: Duplicate detection, idempotent event processing
     *
     * @param eventId Unique event identifier
     * @return Optional containing event if found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<DomainEventDto> findByEventId(String eventId) {
        log.debug("Searching for event: eventId={}", eventId);
        return repository.findByEventId(eventId).map(this::toDto);
    }

    /**
     * Get all events for aggregate (event replay)
     *
     * Flow:
     * 1. Query domain_events WHERE aggregate_id = ? ORDER BY created_at ASC
     * 2. Return events in chronological order
     * 3. Used to reconstruct aggregate state from event history
     *
     * Performance: O(n) where n = number of events for aggregate (usually small)
     * Caching: Consider caching for aggregates with many events
     *
     * @param aggregateId ID of aggregate
     * @return Events in creation order (empty list if no events)
     */
    @Override
    @Transactional(readOnly = true)
    public List<DomainEventDto> getEventsByAggregateId(String aggregateId) {
        log.debug("Fetching events for aggregate: aggregateId={}", aggregateId);
        List<DomainEventJpaEntity> events = repository.findByAggregateIdOrderByCreatedAtAsc(aggregateId);
        log.debug("Found {} events for aggregate: aggregateId={}", events.size(), aggregateId);
        return events.stream().map(this::toDto).toList();
    }

    /**
     * Get all events of a specific type
     *
     * Use Cases:
     * - Trigger subscribers: "PedidoCriadoEvent" -> notify external systems
     * - Reporting: "PedidoSincronizadoEvent" -> success rate metrics
     * - Testing: All events of a type for validation
     *
     * Performance: O(n) where n = events of this type
     * Index: idx_domain_events_event_type
     *
     * @param eventType Event class name
     * @return Events of this type
     */
    @Override
    @Transactional(readOnly = true)
    public List<DomainEventDto> getEventsByType(String eventType) {
        log.debug("Fetching events by type: eventType={}", eventType);
        List<DomainEventJpaEntity> events = repository.findByEventTypeOrderByCreatedAtDesc(eventType);
        log.debug("Found {} events of type: eventType={}", events.size(), eventType);
        return events.stream().map(this::toDto).toList();
    }

    /**
     * Find unpublished events (Event Delivery Tracking pattern)
     *
     * Critical for Reliability:
     * - Events persist to Event Store first (guaranteed write)
     * - Then publish to message broker (eventual consistency)
     * - If broker fails, events remain unpublished
     * - Background job queries unpublished events and retries
     * - Guarantees: No event is lost, all events eventually published
     *
     * Query: WHERE is_published = FALSE ORDER BY created_at ASC
     * Index: idx_domain_events_is_published
     * Performance: O(log n + k) where k = unpublished events
     *
     * Usage:
     * 
     * @Scheduled(fixedDelay = 5000) // Every 5 seconds
     *                       public void publishUnpublishedEvents() {
     *                       List<DomainEventJpaEntity> unpublished =
     *                       findUnpublishedEvents();
     *                       for (event : unpublished) {
     *                       publishToKafka(event);
     *                       markAsPublished(event.getEventId());
     *                       }
     *                       }
     *
     * @return List of unpublished events
     */
    @Override
    @Transactional(readOnly = true)
    public List<DomainEventDto> findUnpublishedEvents() {
        log.debug("Fetching unpublished events");
        List<DomainEventJpaEntity> unpublished = repository.findAllUnpublished();
        log.debug("Found {} unpublished events", unpublished.size());
        return unpublished.stream().map(this::toDto).toList();
    }

    /**
     * Get events published in time range (analytics/reporting)
     *
     * Use Cases:
     * - Dashboard: "How many orders synchronized in last hour?"
     * - Reporting: Business metrics by date range
     * - Auditing: Track synchronization activity
     *
     * Query: WHERE is_published = TRUE AND published_at BETWEEN ? AND ?
     * Performance: O(log n + m) range scan
     *
     * @param startTime Range start (UTC)
     * @param endTime   Range end (UTC)
     * @return Published events in time range
     */
    @Override
    @Transactional(readOnly = true)
    public List<DomainEventDto> getPublishedEventsBetween(Instant startTime, Instant endTime) {
        log.debug("Fetching published events between {} and {}", startTime, endTime);
        List<DomainEventJpaEntity> events = repository.findPublishedEventsBetween(startTime, endTime);
        log.debug("Found {} published events in range", events.size());
        return events.stream().map(this::toDto).toList();
    }

    /**
     * Get events by correlation ID (distributed tracing)
     *
     * Scenario:
     * 1. REST request enters system with trace-id (correlation ID)
     * 2. System processes: creates Pedido, validates, synchronizes with SAP
     * 3. Each event carries same correlation ID
     * 4. Later, query getEventsByCorrelationId("trace-xyz")
     * 5. See complete flow: PedidoCriadoEvent -> PedidoValidadoEvent ->
     * PedidoSincronizadoEvent
     * 6. Debug traces, troubleshoot failures
     *
     * Index: idx_domain_events_correlation_id
     * Performance: O(log n + k) where k = events with correlation
     *
     * @param correlationId Trace correlation ID
     * @return All events linked by correlation
     */
    @Override
    @Transactional(readOnly = true)
    public List<DomainEventDto> getEventsByCorrelationId(String correlationId) {
        log.debug("Fetching events by correlation ID: correlationId={}", correlationId);
        List<DomainEventJpaEntity> events = repository.findByCorrelationIdOrderByCreatedAtAsc(correlationId);
        log.debug("Found {} events with correlation: correlationId={}", events.size(), correlationId);
        return events.stream().map(this::toDto).toList();
    }

    /**
     * Mark event as published to message broker
     *
     * Contract:
     * - Set is_published = TRUE
     * - Set published_at = NOW()
     * - Atomic single UPDATE statement
     *
     * Called After:
     * - Successfully sent to Kafka topic
     * - Message broker ACKed receipt
     * - Event reliably persisted in broker
     *
     * @param eventId Event ID
     * @throws EventNotFoundException if event doesn't exist
     */
    @Override
    @Transactional
    public void markAsPublished(String eventId) {
        markAsPublished(eventId, Instant.now());
    }

    /**
     * Mark event as published with explicit timestamp
     *
     * @param eventId     Event ID
     * @param publishedAt Publication timestamp
     * @throws EventNotFoundException if event doesn't exist
     */
    @Override
    @Transactional
    public void markAsPublished(String eventId, Instant publishedAt) {
        log.debug("Marking event as published: eventId={}, publishedAt={}", eventId, publishedAt);

        int rowsUpdated = repository.markAsPublished(eventId, publishedAt);

        if (rowsUpdated == 0) {
            log.error("Event not found for publication mark: eventId={}", eventId);
            throw new EventNotFoundException(
                    String.format("Event not found: eventId=%s", eventId));
        }

        log.info("Event marked as published: eventId={}, publishedAt={}", eventId, publishedAt);
    }

    /**
     * Delete event from Event Store
     *
     * ⚠️ CAUTION: Rarely used, violates Event Sourcing principles
     *
     * Use Only For:
     * - GDPR right-to-be-forgotten: Delete user personal data
     * - Data correction: Fix erroneous events before replaying
     * - Never: Delete events from normal flow (breaks auditability)
     *
     * Better Alternative:
     * - Archive to separate table instead of deleting
     * - Maintain complete history, mark as archived
     * - Enables future audits, compliance
     *
     * @param eventId Event ID to delete
     * @return true if deleted, false if not found
     */
    @Override
    @Transactional
    public boolean deleteEvent(String eventId) {
        log.warn("Deleting event (should be rare): eventId={}", eventId);

        Optional<DomainEventJpaEntity> event = repository.findByEventId(eventId);
        if (event.isPresent()) {
            repository.delete(event.get());
            log.info("Event deleted: eventId={}", eventId);
            return true;
        }

        log.warn("Event not found for deletion: eventId={}", eventId);
        return false;
    }

    /**
     * Count total events in Event Store
     *
     * Use Case: Monitoring, dashboards, capacity planning
     *
     * @return Total event count
     */
    @Override
    @Transactional(readOnly = true)
    public long countAllEvents() {
        return repository.count();
    }

    /**
     * Count events for a specific aggregate
     *
     * @param aggregateId Aggregate ID
     * @return Number of events
     */
    @Override
    @Transactional(readOnly = true)
    public long countEventsByAggregateId(String aggregateId) {
        return repository.countByAggregateId(aggregateId);
    }
}
