package com.orderintegration.application.service;

import com.orderintegration.application.dto.IdocResponse;
import com.orderintegration.application.port.IdocResponsePort;
import com.orderintegration.application.port.PedidoRepositoryPort;
import com.orderintegration.core.domain.order.ItemPedido;
import com.orderintegration.core.domain.order.Pedido;
import com.orderintegration.core.domain.order.PedidoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdocResponseServiceTest {

    @Mock
    private PedidoRepositoryPort pedidoRepository;

    private IdocResponseService service;

    @BeforeEach
    void setUp() {
        service = new IdocResponseService(pedidoRepository);
    }

    @Test
    void deveProcessarRespostaIdocComSucesso() {
        // Given
        PedidoId pedidoId = PedidoId.gerar();
        Pedido pedido = criarPedidoEmSincronizacao(pedidoId);

        IdocResponse response = IdocResponse.sucesso(
                "idoc-123",
                pedidoId.getValor(),
                "SAP-MSG-001");

        when(pedidoRepository.buscarPorId(pedidoId)).thenReturn(Optional.of(pedido));

        // When
        service.processarRespostaIDocSucesso(response);

        // Then
        verify(pedidoRepository).buscarPorId(pedidoId);
        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).atualizar(pedidoCaptor.capture());

        Pedido pedidoAtualizado = pedidoCaptor.getValue();
        assertEquals("SINCRONIZADO", pedidoAtualizado.getStatus().name());
    }

    @Test
    void deveLancarExcecaoAoProcessarRespostaComPedidoNaoEncontrado() {
        // Given
        PedidoId pedidoId = PedidoId.gerar();
        IdocResponse response = IdocResponse.sucesso(
                "idoc-123",
                pedidoId.getValor(),
                "SAP-MSG-001");

        when(pedidoRepository.buscarPorId(any())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IdocResponsePort.IdocResponseException.class, () -> {
            service.processarRespostaIDocSucesso(response);
        });

        verify(pedidoRepository, never()).atualizar(any());
    }

    @Test
    void deveLancarExcecaoAoProcessarRespostaComPedidoNaoEmSincronizacao() {
        // Given
        PedidoId pedidoId = PedidoId.gerar();
        Pedido pedido = criarPedidoValidado(pedidoId); // Status: VALIDADO (não SINCRONIZANDO)

        IdocResponse response = IdocResponse.sucesso(
                "idoc-123",
                pedidoId.getValor(),
                "SAP-MSG-001");

        when(pedidoRepository.buscarPorId(pedidoId)).thenReturn(Optional.of(pedido));

        // When & Then
        assertThrows(IdocResponsePort.IdocResponseException.class, () -> {
            service.processarRespostaIDocSucesso(response);
        });

        verify(pedidoRepository, never()).atualizar(any());
    }

    @Test
    void deveProcessarErroIdocComSucesso() {
        // Given
        PedidoId pedidoId = PedidoId.gerar();
        Pedido pedido = criarPedidoEmSincronizacao(pedidoId);

        IdocResponse errorResponse = IdocResponse.erro(
                "idoc-456",
                pedidoId.getValor(),
                "E001",
                "Invalid customer code");

        when(pedidoRepository.buscarPorId(pedidoId)).thenReturn(Optional.of(pedido));

        // When
        service.processarRespostaIdocErro(errorResponse);

        // Then
        verify(pedidoRepository).buscarPorId(pedidoId);
        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).atualizar(pedidoCaptor.capture());

        Pedido pedidoAtualizado = pedidoCaptor.getValue();
        assertEquals("ERRO", pedidoAtualizado.getStatus().name());
        assertTrue(pedidoAtualizado.getMensagemErro().contains("E001"));
        assertTrue(pedidoAtualizado.getMensagemErro().contains("Invalid customer code"));
    }

    @Test
    void deveLancarExcecaoAoProcessarErroComPedidoNaoEncontrado() {
        // Given
        PedidoId pedidoId = PedidoId.gerar();
        IdocResponse errorResponse = IdocResponse.erro(
                "idoc-456",
                pedidoId.getValor(),
                "E001",
                "Invalid customer code");

        when(pedidoRepository.buscarPorId(any())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IdocResponsePort.IdocResponseException.class, () -> {
            service.processarRespostaIdocErro(errorResponse);
        });

        verify(pedidoRepository, never()).atualizar(any());
    }

    // Helper methods
    private Pedido criarPedidoValidado(PedidoId pedidoId) {
        List<ItemPedido> itens = List.of(
                ItemPedido.criar("PROD-001", "Notebook", 2, BigDecimal.valueOf(5000)));
        Pedido pedido = Pedido.criar("CLI-001", itens);
        pedido.validar();
        return pedido;
    }

    private Pedido criarPedidoEmSincronizacao(PedidoId pedidoId) {
        Pedido pedido = criarPedidoValidado(pedidoId);
        pedido.iniciarSincronizacao();
        return pedido;
    }
}
