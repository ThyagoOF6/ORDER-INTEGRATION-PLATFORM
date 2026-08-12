package com.orderintegration.adapter.persistence.adapter;

import com.orderintegration.adapter.persistence.entity.DomainEventJpaEntity;
import com.orderintegration.adapter.persistence.repository.DomainEventSpringDataRepository;
import com.orderintegration.application.dto.DomainEventDto;
import com.orderintegration.application.port.DomainEventRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para DomainEventJpaRepositoryAdapter
 * Valida mapeamento DTO <-> JPA Entity e delegação para o Spring Data
 * Repository (Phase 3 - Event Sourcing / Event Store)
 */
@ExtendWith(MockitoExtension.class)
class DomainEventJpaRepositoryAdapterTest {

    @Mock
    private DomainEventSpringDataRepository repository;

    private DomainEventJpaRepositoryAdapter adapter;

    @Test
    void devePersistirEventoEMapearParaDto() {
        adapter = new DomainEventJpaRepositoryAdapter(repository);

        DomainEventDto dto = DomainEventDto.builder()
                .eventId("evt-1")
                .eventType("PedidoCriadoEvent")
                .aggregateId("pedido-1")
                .aggregateType("Pedido")
                .payload(Map.of("codigoCliente", "CLI-001"))
                .createdAt(Instant.now())
                .build();

        DomainEventJpaEntity entitySalva = DomainEventJpaEntity.builder()
                .id(1L)
                .eventId("evt-1")
                .eventType("PedidoCriadoEvent")
                .aggregateId("pedido-1")
                .aggregateType("Pedido")
                .payload(Map.of("codigoCliente", "CLI-001"))
                .createdAt(dto.getCreatedAt())
                .build();

        when(repository.save(any(DomainEventJpaEntity.class))).thenReturn(entitySalva);

        DomainEventDto resultado = adapter.persistEvent(dto);

        assertEquals("evt-1", resultado.getEventId());
        assertEquals("pedido-1", resultado.getAggregateId());

        ArgumentCaptor<DomainEventJpaEntity> captor = ArgumentCaptor.forClass(DomainEventJpaEntity.class);
        verify(repository, times(1)).save(captor.capture());
        assertEquals("evt-1", captor.getValue().getEventId());
    }

    @Test
    void deveBuscarEventoPorIdEMapearParaDto() {
        adapter = new DomainEventJpaRepositoryAdapter(repository);

        DomainEventJpaEntity entity = DomainEventJpaEntity.builder()
                .eventId("evt-2")
                .aggregateId("pedido-2")
                .aggregateType("Pedido")
                .eventType("PedidoSincronizadoEvent")
                .build();

        when(repository.findByEventId("evt-2")).thenReturn(Optional.of(entity));

        Optional<DomainEventDto> resultado = adapter.findByEventId("evt-2");

        assertTrue(resultado.isPresent());
        assertEquals("pedido-2", resultado.get().getAggregateId());
    }

    @Test
    void deveRetornarVazioQuandoEventoNaoEncontrado() {
        adapter = new DomainEventJpaRepositoryAdapter(repository);

        when(repository.findByEventId("inexistente")).thenReturn(Optional.empty());

        Optional<DomainEventDto> resultado = adapter.findByEventId("inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveListarEventosPorAggregateIdEmOrdemCronologica() {
        adapter = new DomainEventJpaRepositoryAdapter(repository);

        DomainEventJpaEntity e1 = DomainEventJpaEntity.builder().eventId("e1").aggregateId("pedido-3").build();
        DomainEventJpaEntity e2 = DomainEventJpaEntity.builder().eventId("e2").aggregateId("pedido-3").build();

        when(repository.findByAggregateIdOrderByCreatedAtAsc("pedido-3")).thenReturn(List.of(e1, e2));

        List<DomainEventDto> eventos = adapter.getEventsByAggregateId("pedido-3");

        assertEquals(2, eventos.size());
        assertEquals("e1", eventos.get(0).getEventId());
        assertEquals("e2", eventos.get(1).getEventId());
    }

    @Test
    void deveMarcarEventoComoPublicado() {
        adapter = new DomainEventJpaRepositoryAdapter(repository);

        when(repository.markAsPublished(eq("evt-4"), any(Instant.class))).thenReturn(1);

        adapter.markAsPublished("evt-4");

        verify(repository, times(1)).markAsPublished(eq("evt-4"), any(Instant.class));
    }

    @Test
    void deveLancarExcecaoAoMarcarEventoInexistenteComoPublicado() {
        adapter = new DomainEventJpaRepositoryAdapter(repository);

        when(repository.markAsPublished(eq("evt-inexistente"), any(Instant.class))).thenReturn(0);

        assertThrows(DomainEventRepositoryPort.EventNotFoundException.class,
                () -> adapter.markAsPublished("evt-inexistente"));
    }

    @Test
    void deveBuscarEventosNaoPublicados() {
        adapter = new DomainEventJpaRepositoryAdapter(repository);

        DomainEventJpaEntity naoPublicado = DomainEventJpaEntity.builder()
                .eventId("evt-5")
                .isPublished(false)
                .build();

        when(repository.findAllUnpublished()).thenReturn(List.of(naoPublicado));

        List<DomainEventDto> resultado = adapter.findUnpublishedEvents();

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).getIsPublished());
    }

    @Test
    void deveContarEventosPorAggregateId() {
        adapter = new DomainEventJpaRepositoryAdapter(repository);

        when(repository.countByAggregateId("pedido-6")).thenReturn(3L);

        long total = adapter.countEventsByAggregateId("pedido-6");

        assertEquals(3L, total);
    }
}
