package com.orderintegration.application.service;

import com.orderintegration.application.dto.DomainEventDto;
import com.orderintegration.application.port.DomainEventRepositoryPort;
import com.orderintegration.core.domain.order.ItemPedido;
import com.orderintegration.core.domain.order.Pedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para EventPublisherService
 * Valida persistência de eventos de domínio no Event Store (Phase 3)
 */
@ExtendWith(MockitoExtension.class)
class EventPublisherServiceTest {

    @Mock
    private DomainEventRepositoryPort domainEventRepository;

    @Test
    void devePersistirEventoDeCriacaoDoPedido() {
        // Given
        EventPublisherService service = new EventPublisherService(domainEventRepository);
        ItemPedido item = ItemPedido.criar("PROD-001", "Produto Teste", 1, BigDecimal.TEN);
        Pedido pedido = Pedido.criar("CLI-001", List.of(item));

        assertEquals(1, pedido.getEventos().size());

        // When
        service.publicarEventos(pedido);

        // Then
        ArgumentCaptor<DomainEventDto> captor = ArgumentCaptor.forClass(DomainEventDto.class);
        verify(domainEventRepository, times(1)).persistEvent(captor.capture());

        DomainEventDto dto = captor.getValue();
        assertEquals(pedido.getPedidoId(), dto.getAggregateId());
        assertEquals("Pedido", dto.getAggregateType());
        assertTrue(dto.getEventType().contains("PedidoCriadoEvent"));
        assertNotNull(dto.getPayload());
        assertEquals("CLI-001", dto.getPayload().get("codigoCliente"));
    }

    @Test
    void deveLimparEventosDoPedidoAposPublicar() {
        // Given
        EventPublisherService service = new EventPublisherService(domainEventRepository);
        ItemPedido item = ItemPedido.criar("PROD-001", "Produto Teste", 1, BigDecimal.TEN);
        Pedido pedido = Pedido.criar("CLI-001", List.of(item));

        // When
        service.publicarEventos(pedido);

        // Then
        assertTrue(pedido.getEventos().isEmpty());
    }

    @Test
    void naoDevePersistirNadaQuandoNaoHaEventosPendentes() {
        // Given
        EventPublisherService service = new EventPublisherService(domainEventRepository);
        ItemPedido item = ItemPedido.criar("PROD-001", "Produto Teste", 1, BigDecimal.TEN);
        Pedido pedido = Pedido.criar("CLI-001", List.of(item));
        pedido.limparEventos();

        // When
        service.publicarEventos(pedido);

        // Then
        verify(domainEventRepository, never()).persistEvent(any());
    }

    @Test
    void devePersistirEventoDeErroSincronizacaoComPayloadCompleto() {
        // Given
        EventPublisherService service = new EventPublisherService(domainEventRepository);
        ItemPedido item = ItemPedido.criar("PROD-001", "Produto Teste", 1, BigDecimal.TEN);
        Pedido pedido = Pedido.criar("CLI-001", List.of(item));
        pedido.limparEventos();
        pedido.validar();
        pedido.iniciarSincronizacao();
        pedido.registrarErro("Falha ao conectar com SAP");

        // When
        service.publicarEventos(pedido);

        // Then
        ArgumentCaptor<DomainEventDto> captor = ArgumentCaptor.forClass(DomainEventDto.class);
        verify(domainEventRepository, times(1)).persistEvent(captor.capture());

        DomainEventDto dto = captor.getValue();
        assertTrue(dto.getEventType().contains("PedidoErroSincronizacaoEvent"));
        assertEquals("Falha ao conectar com SAP", dto.getPayload().get("mensagemErro"));
    }
}
