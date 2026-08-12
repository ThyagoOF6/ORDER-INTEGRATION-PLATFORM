package com.orderintegration.adapter.persistence.repository;

import com.orderintegration.adapter.persistence.entity.DomainEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * DomainEventSpringDataRepository - Spring Data JPA interface for Event Store
 *
 * Purpose:
 * - Provide CRUD and query operations on domain_events table
 * - Leverage Spring Data JPA for automatic SQL generation
 * - Support custom queries via @Query annotations
 * - Enable JSONB queries on PostgreSQL for efficient filtering
 *
 * Repository Methods:
 * - Basic CRUD: save(), findById(), delete()
 * - Bulk: saveAll(), deleteInBatch()
 * - Custom: Named query methods + @Query annotations
 *
 * @author Thyago Oliveira Ferreira
 * @version 1.0 - Phase 3 (Event Sourcing)
 * @since 2026-08-12
 */
@Repository
public interface DomainEventSpringDataRepository extends JpaRepository<DomainEventJpaEntity, Long> {

    /**
     * Find event by unique event ID
     *
     * Query: SELECT * FROM domain_events WHERE event_id = ?
     * Index: idx_domain_events_event_id (unique)
     * Performance: O(log n) - indexed lookup
     *
     * @param eventId Unique event identifier
     * @return Optional containing event if found
     */
    Optional<DomainEventJpaEntity> findByEventId(String eventId);

    /**
     * Find all events for an aggregate in chronological order
     *
     * Query: SELECT * FROM domain_events WHERE aggregate_id = ? ORDER BY created_at
     * ASC
     * Index: idx_domain_events_aggregate_id
     * Usage: Aggregate reconstruction via event replay
     * Performance: O(n) - linear scan of aggregate's events (typically small)
     *
     * @param aggregateId Aggregate ID (e.g., "pedido-123")
     * @return List of events in creation order (oldest first)
     */
    List<DomainEventJpaEntity> findByAggregateIdOrderByCreatedAtAsc(String aggregateId);

    /**
     * Find all events of a specific type
     *
     * Query: SELECT * FROM domain_events WHERE event_type = ?
     * Index: idx_domain_events_event_type
     * Usage: Trigger subscribers, analytics, reporting
     * Performance: O(log n + m) where m = number of events of this type
     *
     * @param eventType Event class name (e.g., "PedidoCriadoEvent")
     * @return List of events of this type
     */
    List<DomainEventJpaEntity> findByEventTypeOrderByCreatedAtDesc(String eventType);

    /**
     * Find unpublished events (awaiting message broker delivery)
     *
     * Query: SELECT * FROM domain_events WHERE is_published = FALSE ORDER BY
     * created_at ASC
     * Index: idx_domain_events_is_published
     * Usage: Event Delivery Tracking pattern
     * Performance: O(log n + k) where k = unpublished events (usually small)
     *
     * @return List of unpublished events in creation order
     */
    @Query("SELECT e FROM DomainEventJpaEntity e WHERE e.isPublished = FALSE ORDER BY e.createdAt ASC")
    List<DomainEventJpaEntity> findAllUnpublished();

    /**
     * Find events published in a time range
     *
     * Query: SELECT * FROM domain_events
     * WHERE is_published = TRUE
     * AND published_at >= ? AND published_at <= ?
     * ORDER BY published_at DESC
     * Indexes: idx_domain_events_is_published, (implied created_at index)
     * Usage: Analytics, reporting, dashboards
     * Performance: O(log n + m) range scan
     *
     * @param startTime Start of time range (UTC)
     * @param endTime   End of time range (UTC)
     * @return List of published events in time range
     */
    @Query("SELECT e FROM DomainEventJpaEntity e " +
            "WHERE e.isPublished = TRUE " +
            "AND e.publishedAt >= :startTime AND e.publishedAt <= :endTime " +
            "ORDER BY e.publishedAt DESC")
    List<DomainEventJpaEntity> findPublishedEventsBetween(
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Find events by correlation ID (distributed tracing)
     *
     * Query: SELECT * FROM domain_events WHERE correlation_id = ?
     * Index: idx_domain_events_correlation_id
     * Usage: Trace request flow across systems
     * Performance: O(log n + k) where k = events with this correlation
     *
     * @param correlationId Correlation ID
     * @return List of events linked by correlation
     */
    List<DomainEventJpaEntity> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);

    /**
     * Find events by causation ID (cause-effect linking)
     *
     * Query: SELECT * FROM domain_events WHERE causation_id = ?
     * Index: (none - rarely used, can add if needed)
     * Usage: Understand cause-effect chains
     * Performance: O(n) full table scan or with index O(log n + k)
     *
     * @param causationId Causation ID
     * @return List of events caused by the causation event
     */
    List<DomainEventJpaEntity> findByCausationIdOrderByCreatedAtAsc(String causationId);

    /**
     * Find events for multiple aggregates (batch processing)
     *
     * Query: SELECT * FROM domain_events WHERE aggregate_id IN (...)
     * Usage: Multi-aggregate replay, batch operations
     * Performance: O(log n + m) where m = events in aggregate list
     *
     * @param aggregateIds List of aggregate IDs
     * @return List of events for all aggregates
     */
    @Query("SELECT e FROM DomainEventJpaEntity e " +
            "WHERE e.aggregateId IN :aggregateIds " +
            "ORDER BY e.aggregateId, e.createdAt ASC")
    List<DomainEventJpaEntity> findEventsForAggregates(
            @Param("aggregateIds") List<String> aggregateIds);

    /**
     * Mark an event as published
     *
     * Query: UPDATE domain_events SET is_published = TRUE, published_at = ? WHERE
     * event_id = ?
     * Indexes: idx_domain_events_event_id (for WHERE clause)
     * Performance: O(1) - direct row update
     *
     * @param eventId     Event ID to update
     * @param publishedAt Publication timestamp
     * @return Number of rows updated (0 or 1)
     */
    @Modifying
    @Query("UPDATE DomainEventJpaEntity e " +
            "SET e.isPublished = TRUE, e.publishedAt = :publishedAt " +
            "WHERE e.eventId = :eventId")
    int markAsPublished(
            @Param("eventId") String eventId,
            @Param("publishedAt") Instant publishedAt);

    /**
     * Mark multiple events as published (batch)
     *
     * Query: UPDATE domain_events SET is_published = TRUE, published_at = ? WHERE
     * event_id IN (...)
     * Performance: O(log n + k) where k = events to update
     *
     * @param eventIds    List of event IDs
     * @param publishedAt Publication timestamp
     * @return Number of rows updated
     */
    @Modifying
    @Query("UPDATE DomainEventJpaEntity e " +
            "SET e.isPublished = TRUE, e.publishedAt = :publishedAt " +
            "WHERE e.eventId IN :eventIds")
    int markAsPublishedBatch(
            @Param("eventIds") List<String> eventIds,
            @Param("publishedAt") Instant publishedAt);

    /**
     * Find events by aggregate type (all aggregates of a type)
     *
     * Query: SELECT * FROM domain_events WHERE aggregate_type = ?
     * Index: idx_domain_events_aggregate_type
     * Usage: Reporting all events for a domain entity type
     * Performance: O(log n + m) where m = events of this type
     *
     * @param aggregateType Aggregate type (e.g., "Pedido")
     * @return List of events for this aggregate type
     */
    List<DomainEventJpaEntity> findByAggregateTypeOrderByCreatedAtDesc(String aggregateType);

    /**
     * Check if event exists by ID (efficient count)
     *
     * Query: SELECT COUNT(*) FROM domain_events WHERE event_id = ?
     * Performance: O(1) - indexed lookup
     *
     * @param eventId Event ID
     * @return true if event exists
     */
    boolean existsByEventId(String eventId);

    /**
     * Count events for an aggregate
     *
     * Query: SELECT COUNT(*) FROM domain_events WHERE aggregate_id = ?
     * Performance: O(log n)
     *
     * @param aggregateId Aggregate ID
     * @return Number of events
     */
    long countByAggregateId(String aggregateId);

    /**
     * Count unpublished events
     *
     * Query: SELECT COUNT(*) FROM domain_events WHERE is_published = FALSE
     * Usage: Monitoring, alerting on pending events
     * Performance: O(log n)
     *
     * @return Number of unpublished events
     */
    long countByIsPublishedFalse();

    /**
     * Find oldest unpublished event (for priority queue retry)
     *
     * Query: SELECT * FROM domain_events WHERE is_published = FALSE ORDER BY
     * created_at ASC LIMIT 1
     * Usage: Process oldest events first (FIFO ordering)
     * Performance: O(log n)
     *
     * @return Optional containing oldest unpublished event
     */
    @Query("SELECT e FROM DomainEventJpaEntity e " +
            "WHERE e.isPublished = FALSE " +
            "ORDER BY e.createdAt ASC " +
            "LIMIT 1")
    Optional<DomainEventJpaEntity> findOldestUnpublished();

    /**
     * Delete events older than a timestamp (archival/cleanup)
     *
     * Query: DELETE FROM domain_events WHERE created_at < ?
     * Usage: Archive old events to separate table, then delete
     * Performance: O(log n + k) where k = events to delete
     *
     * @param beforeTime Timestamp threshold
     * @return Number of deleted events
     */
    @Modifying
    @Query("DELETE FROM DomainEventJpaEntity e WHERE e.createdAt < :beforeTime")
    int deleteEventsBefore(@Param("beforeTime") Instant beforeTime);
}
