package com.orderintegration.adapter.messaging.sap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderintegration.application.dto.IdocResponse;
import com.orderintegration.application.port.IdocResponsePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listener para erros de iDoc
 * 
 * Fluxo:
 * 1. iDoc é publicado via IdocPublisher → fila SAP
 * 2. SAP processa o iDoc mas ocorre erro
 * 3. Erro é publicado em fila de erros (error.queue)
 * 4. Este listener consome a mensagem de erro
 * 5. Atualiza Pedido: SINCRONIZANDO → ERRO com mensagem
 * 
 * Implementação: Kafka topic "sap-idoc-error" (Phase 3)
 */
@Component
public class ErrorQueueListener {

    private static final Logger logger = LoggerFactory.getLogger(ErrorQueueListener.class);
    private final IdocResponsePort idocResponsePort;
    private final ObjectMapper objectMapper;

    public ErrorQueueListener(IdocResponsePort idocResponsePort, ObjectMapper objectMapper) {
        this.idocResponsePort = idocResponsePort;
        this.objectMapper = objectMapper;
    }

    /**
     * Consome mensagens JSON do tópico Kafka de erros de iDoc e delega o
     * processamento para processarErro(IdocResponse)
     */
    @KafkaListener(topics = "${messaging.topics.idoc-response-error:sap-idoc-error}", groupId = "${spring.kafka.consumer.group-id:order-integration-group}")
    public void onMessage(String mensagemJson) {
        try {
            IdocResponse errorResponse = objectMapper.readValue(mensagemJson, IdocResponse.class);
            processarErro(errorResponse);
        } catch (Exception e) {
            logger.error("Falha ao desserializar mensagem de erro iDoc: {}", mensagemJson, e);
        }
    }

    /**
     * Processa mensagem de erro de iDoc da fila
     */
    public void processarErro(IdocResponse errorResponse) {
        logger.warn("Recebendo erro de iDoc: {}", errorResponse);

        try {
            // Validar resposta
            if (errorResponse == null || errorResponse.getPedidoId() == null) {
                logger.error("IdocResponse de erro inválida: campos obrigatórios ausentes");
                return;
            }

            // Validar que contém informações de erro
            if (errorResponse.getErrorCode() == null || errorResponse.getErrorMessage() == null) {
                logger.error("IdocResponse de erro sem informações de erro para pedidoId={}",
                        errorResponse.getPedidoId());
                return;
            }

            // Processar erro via port
            idocResponsePort.processarRespostaIdocErro(errorResponse);

            logger.info("Erro iDoc registrado para pedidoId={}", errorResponse.getPedidoId());

        } catch (IdocResponsePort.IdocResponseException e) {
            logger.error("Erro ao processar erro de iDoc: {}", errorResponse, e);
            // Em produção: implementar dead-letter queue ou alertas
        } catch (Exception e) {
            logger.error("Erro inesperado ao processar erro de iDoc", e);
        }
    }
}
