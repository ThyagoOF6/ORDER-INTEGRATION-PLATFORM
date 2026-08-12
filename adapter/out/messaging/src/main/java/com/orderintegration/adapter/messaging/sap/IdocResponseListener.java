package com.orderintegration.adapter.messaging.sap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderintegration.application.dto.IdocResponse;
import com.orderintegration.application.port.IdocResponsePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listener para respostas de iDoc bem-sucedidas
 * 
 * Fluxo:
 * 1. iDoc é publicado via IdocPublisher → fila SAP
 * 2. SAP processa o iDoc e envia resposta
 * 3. Resposta é publicada em fila de respostas (response.queue)
 * 4. Este listener consome a mensagem
 * 5. Atualiza Pedido: SINCRONIZANDO → SINCRONIZADO
 * 
 * Implementação: Kafka topic "sap-idoc-response" (Phase 3)
 */
@Component
public class IdocResponseListener {

    private static final Logger logger = LoggerFactory.getLogger(IdocResponseListener.class);
    private final IdocResponsePort idocResponsePort;
    private final ObjectMapper objectMapper;

    public IdocResponseListener(IdocResponsePort idocResponsePort, ObjectMapper objectMapper) {
        this.idocResponsePort = idocResponsePort;
        this.objectMapper = objectMapper;
    }

    /**
     * Consome mensagens JSON do t\u00f3pico Kafka de respostas iDoc bem-sucedidas
     * e delega o processamento para o m\u00e9todo processarResposta(IdocResponse)
     */
    @KafkaListener(topics = "${messaging.topics.idoc-response-success:sap-idoc-response}", groupId = "${spring.kafka.consumer.group-id:order-integration-group}")
    public void onMessage(String mensagemJson) {
        try {
            IdocResponse response = objectMapper.readValue(mensagemJson, IdocResponse.class);
            processarResposta(response);
        } catch (Exception e) {
            logger.error("Falha ao desserializar mensagem de resposta iDoc: {}", mensagemJson, e);
        }
    }

    /**
     * Processa mensagem de resposta de iDoc da fila
     */
    public void processarResposta(IdocResponse response) {
        logger.info("Recebendo resposta iDoc: {}", response);

        try {
            // Validar resposta
            if (response == null || response.getPedidoId() == null) {
                logger.error("IdocResponse inválida: campos obrigatórios ausentes");
                return;
            }

            // Processar com IdocResponseService via port
            idocResponsePort.processarRespostaIDocSucesso(response);

            logger.info("Resposta iDoc processada com sucesso para pedidoId={}",
                    response.getPedidoId());

        } catch (IdocResponsePort.IdocResponseException e) {
            logger.error("Erro ao processar resposta iDoc: {}", response, e);
            // Em produção: enviar para error queue ou retry topic
        } catch (Exception e) {
            logger.error("Erro inesperado ao processar resposta iDoc", e);
        }
    }
}
