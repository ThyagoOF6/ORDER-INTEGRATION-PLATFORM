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
class ErrorQueueListenerTest {

    @Mock
    private IdocResponsePort idocResponsePort;

    private ErrorQueueListener listener;

    @BeforeEach
    void setUp() {
        listener = new ErrorQueueListener(idocResponsePort);
    }

    @Test
    void deveProcessarErroIdoc() {
        // Given
        IdocResponse errorResponse = new IdocResponse(
                "idoc-456",
                "pedido-789",
                "REJEITADO",
                null,
                "E001",
                "Invalid customer code",
                Instant.now());

        // When
        listener.processarErro(errorResponse);

        // Then
        verify(idocResponsePort).processarRespostaIdocErro(errorResponse);
    }

    @Test
    void naoDeveProcessarErroNulo() {
        // When
        listener.processarErro(null);

        // Then
        verify(idocResponsePort, never()).processarRespostaIdocErro(any());
    }

    @Test
    void naoDeveProcessarErroSemPedidoId() {
        // Given
        IdocResponse errorResponse = new IdocResponse();
        errorResponse.setIdocId("idoc-456");
        errorResponse.setPedidoId(null);

        // When
        listener.processarErro(errorResponse);

        // Then
        verify(idocResponsePort, never()).processarRespostaIdocErro(any());
    }

    @Test
    void naoDeveProcessarErroSemCodigoErro() {
        // Given
        IdocResponse errorResponse = new IdocResponse(
                "idoc-456",
                "pedido-789",
                "REJEITADO",
                null,
                null, // Missing error code
                "Invalid customer code",
                Instant.now());

        // When
        listener.processarErro(errorResponse);

        // Then
        verify(idocResponsePort, never()).processarRespostaIdocErro(any());
    }

    @Test
    void naoDeveProcessarErroSemMensagemErro() {
        // Given
        IdocResponse errorResponse = new IdocResponse(
                "idoc-456",
                "pedido-789",
                "REJEITADO",
                null,
                "E001",
                null, // Missing error message
                Instant.now());

        // When
        listener.processarErro(errorResponse);

        // Then
        verify(idocResponsePort, never()).processarRespostaIdocErro(any());
    }

    @Test
    void deveHandleExcecaoAoProcessarErro() {
        // Given
        IdocResponse errorResponse = new IdocResponse(
                "idoc-456",
                "pedido-789",
                "REJEITADO",
                null,
                "E001",
                "Invalid customer code",
                Instant.now());

        doThrow(new IdocResponsePort.IdocResponseException("Pedido não encontrado"))
                .when(idocResponsePort).processarRespostaIdocErro(errorResponse);

        // When - deve não lançar exceção
        listener.processarErro(errorResponse);

        // Then
        verify(idocResponsePort).processarRespostaIdocErro(errorResponse);
    }
}
