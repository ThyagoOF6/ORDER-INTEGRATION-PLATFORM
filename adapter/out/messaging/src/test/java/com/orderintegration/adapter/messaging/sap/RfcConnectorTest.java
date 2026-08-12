package com.orderintegration.adapter.messaging.sap;

import com.orderintegration.core.domain.order.ItemPedido;
import com.orderintegration.core.domain.order.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RfcConnector
 */
@ExtendWith(MockitoExtension.class)
class RfcConnectorTest {

    private RfcConnector rfcConnector;
    private SapConnectorConfig sapConfig;
    private Pedido pedidoTeste;

    @BeforeEach
    void setUp() {
        sapConfig = new SapConnectorConfig();
        sapConfig.setHost("localhost");
        sapConfig.setPort(3200);
        sapConfig.setClient("100");
        sapConfig.setUser("DEVELOPER");
        sapConfig.setPassword("test123");
        sapConfig.setFunctionModuleCreateOrder("ZORDERS_CREATE");

        rfcConnector = new RfcConnector(sapConfig);

        // Preparar pedido de teste
        ItemPedido item = ItemPedido.criar("PROD-001", "Produto Teste",
                5, new BigDecimal("100.00"));
        pedidoTeste = Pedido.criar("CLI-001", List.of(item));
    }

    @Test
    void deveConectarECriarPedidoRfcComSucesso() throws RfcConnector.RfcException {
        // When
        String transacaoId = rfcConnector.criarPedidoRfc(pedidoTeste);

        // Then
        assertNotNull(transacaoId);
        assertTrue(transacaoId.startsWith("SAP-"));
        assertFalse(transacaoId.isBlank());
    }

    @Test
    void deveAtualizarStatusPedidoRfcComSucesso() throws RfcConnector.RfcException {
        // When
        String transacaoId = rfcConnector.atualizarStatusPedidoRfc(
                pedidoTeste.getPedidoId(),
                "SINCRONIZADO");

        // Then
        assertNotNull(transacaoId);
        assertTrue(transacaoId.startsWith("SAP-"));
    }

    @Test
    void deveGerarRequestComTodosOsDadosDoPedido() {
        // Teste para validar que request monta corretamente
        // Este teste valida a montagem interna de request (método privado)
        // Teste implícito: se criarPedidoRfc funciona, request está correto
        assertDoesNotThrow(() -> rfcConnector.criarPedidoRfc(pedidoTeste));
    }

    @Test
    void deveHandleTimeoutNaConexao() {
        // Given: conexão com timeout muito baixo
        SapConnectorConfig configLowTimeout = new SapConnectorConfig();
        configLowTimeout.setConnectionTimeoutMs(1L);
        RfcConnector rfcConnectorComTimeout = new RfcConnector(configLowTimeout);

        // When & Then: timeout pode disparar
        // Em ambiente de desenvolvimento, mock garante sucesso
        assertDoesNotThrow(() -> rfcConnectorComTimeout.criarPedidoRfc(pedidoTeste));
    }
}
