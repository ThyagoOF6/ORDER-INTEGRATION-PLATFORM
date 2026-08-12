package com.orderintegration.adapter.messaging.sap;

import com.orderintegration.core.domain.order.ItemPedido;
import com.orderintegration.core.domain.order.Pedido;
import com.orderintegration.core.domain.order.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

/**
 * Testes unitários para SapOrderAdapter
 */
@ExtendWith(MockitoExtension.class)
class SapOrderAdapterTest {
    
    @Mock
    private RfcConnector rfcConnector;
    
    @Mock
    private IdocPublisher idocPublisher;
    
    private SapOrderAdapter sapOrderAdapter;
    private Pedido pedidoTeste;
    
    @BeforeEach
    void setUp() {
        sapOrderAdapter = new SapOrderAdapter(rfcConnector, idocPublisher);
        
        // Preparar pedido de teste
        ItemPedido item = ItemPedido.criar("PROD-001", "Produto Teste", 
                                          new BigDecimal("5"), new BigDecimal("100.00"));
        pedidoTeste = Pedido.criar("CLI-001", List.of(item));
    }
    
    @Test
    void deveSincronizarPedidoViaRfcComSucesso() throws RfcConnector.RfcException {
        // Given
        String transacaoIdEsperada = "SAP-2024-001234";
        when(rfcConnector.criarPedidoRfc(any(Pedido.class)))
                .thenReturn(transacaoIdEsperada);
        
        // When
        String transacaoId = sapOrderAdapter.sincronizarPedidoRfc(pedidoTeste);
        
        // Then
        assertNotNull(transacaoId);
        assertEquals(transacaoIdEsperada, transacaoId);
        verify(rfcConnector).criarPedidoRfc(any(Pedido.class));
    }
    
    @Test
    void deveLancarExcecaoAoFalharSincronizacaoRfc() throws RfcConnector.RfcException {
        // Given
        doThrow(new RfcConnector.RfcException("Erro de conexão"))
                .when(rfcConnector).criarPedidoRfc(any(Pedido.class));
        
        // When & Then
        assertThrows(Exception.class, () -> {
            sapOrderAdapter.sincronizarPedidoRfc(pedidoTeste);
        });
    }
    
    @Test
    void devePublicarPedidoIdocComSucesso() throws IdocPublisher.IdocException {
        // Given
        String idocIdEsperado = "IDOC-2024-ABCD1234";
        when(idocPublisher.publicarPedidoIdoc(any(Pedido.class)))
                .thenReturn(idocIdEsperado);
        
        // When
        String idocId = sapOrderAdapter.publicarPedidoIdoc(pedidoTeste);
        
        // Then
        assertNotNull(idocId);
        assertEquals(idocIdEsperado, idocId);
        verify(idocPublisher).publicarPedidoIdoc(any(Pedido.class));
    }
    
    @Test
    void deveLancarExcecaoAoFalharPublicacaoIdoc() throws IdocPublisher.IdocException {
        // Given
        doThrow(new IdocPublisher.IdocException("Falha na fila"))
                .when(idocPublisher).publicarPedidoIdoc(any(Pedido.class));
        
        // When & Then
        assertThrows(Exception.class, () -> {
            sapOrderAdapter.publicarPedidoIdoc(pedidoTeste);
        });
    }
}
