package com.orderintegration.adapter.messaging.sap;

import com.orderintegration.application.dto.IdocResponse;
import com.orderintegration.application.port.IdocResponsePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Implementations:
 * - Kafka topic: sap-idoc-response
 * - RabbitMQ queue: sap.idoc.response
 * - Azure Service Bus queue: sap-idoc-response
 */
@Component
public class IdocResponseListener {

    private static final Logger logger = LoggerFactory.getLogger(IdocResponseListener.class);
    private final IdocResponsePort idocResponsePort;

    public IdocResponseListener(IdocResponsePort idocResponsePort) {
        this.idocResponsePort = idocResponsePort;
    }

    /**
     * Processa mensagem de resposta de iDoc da fila
     * 
     * Não implementar como @KafkaListener, @RabbitListener
     * ou @ServiceBusQueueListener aqui
     * para manter desacoplamento. Implementações concretas de message broker
     * criarão as classes específicas que chamarão este método.
     * 
     * Exemplo de uso via Kafka:
     * 
     * @KafkaListener(topics = "sap-idoc-response", groupId = "order-integration")
     *                       public void onIdocResponse(String message) {
     *                       IdocResponse response = objectMapper.readValue(message,
     *                       IdocResponse.class);
     *                       this.processarResposta(response);
     *                       }
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
