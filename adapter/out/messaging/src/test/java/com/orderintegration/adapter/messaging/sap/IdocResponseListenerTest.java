package com.orderintegration.adapter.messaging.sap;

import com.orderintegration.application.dto.IdocResponse;
import com.orderintegration.application.port.IdocResponsePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdocResponseListenerTest {

    @Mock
    private IdocResponsePort idocResponsePort;

    private IdocResponseListener listener;

    @BeforeEach
    void setUp() {
        listener = new IdocResponseListener(idocResponsePort);
    }

    @Test
    void deveProcessarRespostaBemSucedida() {
        // Given
        IdocResponse response = new IdocResponse(
                "idoc-123",
                "pedido-456",
                "PROCESSADO",
                "SAP-MSG-001",
                null,
                null,
                Instant.now());

        // When
        listener.processarResposta(response);

        // Then
        verify(idocResponsePort).processarRespostaIDocSucesso(response);
    }

    @Test
    void naoDeveProcessarRespostaNula() {
        // When
        listener.processarResposta(null);

        // Then
        verify(idocResponsePort, never()).processarRespostaIDocSucesso(any());
    }

    @Test
    void naoDeveProcessarRespostaSemPedidoId() {
        // Given
        IdocResponse response = new IdocResponse();
        response.setIdocId("idoc-123");
        response.setPedidoId(null); // Missing required field

        // When
        listener.processarResposta(response);

        // Then
        verify(idocResponsePort, never()).processarRespostaIDocSucesso(any());
    }

    @Test
    void deveHandleExcecaoAoProcessar() {
        // Given
        IdocResponse response = new IdocResponse(
                "idoc-123",
                "pedido-456",
                "PROCESSADO",
                "SAP-MSG-001",
                null,
                null,
                Instant.now());

        doThrow(new IdocResponsePort.IdocResponseException("Pedido não encontrado"))
                .when(idocResponsePort).processarRespostaIDocSucesso(response);

        // When - deve não lançar exceção
        listener.processarResposta(response);

        // Then
        verify(idocResponsePort).processarRespostaIDocSucesso(response);
    }
}
