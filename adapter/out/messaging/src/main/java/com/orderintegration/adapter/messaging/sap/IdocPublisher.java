package com.orderintegration.adapter.messaging.sap;

import com.orderintegration.core.domain.order.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * iDoc Publisher: Integração assíncrona com SAP via iDoc (Intermediate
 * Document)
 * Responsável por: gerar e publicar iDocs para fila de mensagens
 * 
 * iDoc é formato padrão SAP para integração B2B
 * Fluxo: Pedido → iDoc XML → Queue/Broker → SAP
 */
@Component
public class IdocPublisher {

    private static final Logger logger = LoggerFactory.getLogger(IdocPublisher.class);

    private final SapConnectorConfig config;

    public IdocPublisher(SapConnectorConfig config) {
        this.config = config;
    }

    /**
     * Publica um iDoc ORDERS no formato padrão SAP
     * Serializa o pedido para XML iDoc e envia para fila
     * 
     * @param pedido o pedido a publicar
     * @return ID único do iDoc gerado
     * @throws IdocException em caso de erro
     */
    public String publicarPedidoIdoc(Pedido pedido) throws IdocException {
        logger.info("Publicando iDoc para pedido: {}", pedido.getPedidoId());

        try {
            String idocXml = gerarIdocXml(pedido);
            String idocId = UUID.randomUUID().toString();

            // Publicar em fila/broker
            enviarParaFila(idocId, idocXml);

            logger.info("iDoc publicado com sucesso. ID: {}", idocId);
            return idocId;

        } catch (Exception e) {
            logger.error("Falha ao publicar iDoc para pedido: {}", pedido.getPedidoId(), e);
            throw new IdocException("Falha ao publicar iDoc: " + e.getMessage(), e);
        }
    }

    /**
     * Gera XML iDoc no formato SAP ORDERS
     * Estrutura EDI padrão para integração de pedidos
     */
    private String gerarIdocXml(Pedido pedido) {
        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<IDOC>\n");

        // Segment de controle
        xml.append("  <CONTROL>\n");
        xml.append("    <MESTYP>").append(config.getIdocMessageType()).append("</MESTYP>\n");
        xml.append("    <PROCESS_CODE>").append(config.getIdocProcessCode()).append("</PROCESS_CODE>\n");
        xml.append("    <LOGICAL_PORT>").append(config.getIdocPortDest()).append("</LOGICAL_PORT>\n");
        xml.append("    <TIMESTAMP>").append(Instant.now()).append("</TIMESTAMP>\n");
        xml.append("  </CONTROL>\n");

        // Segment de cabeçalho
        xml.append("  <HEADER>\n");
        xml.append("    <PEDIDO_ID>").append(pedido.getPedidoId()).append("</PEDIDO_ID>\n");
        xml.append("    <CLIENTE_ID>").append(pedido.getCodigoCliente()).append("</CLIENTE_ID>\n");
        xml.append("    <STATUS>").append(pedido.getStatus().name()).append("</STATUS>\n");
        xml.append("    <VALOR_TOTAL>").append(pedido.getValorTotal()).append("</VALOR_TOTAL>\n");
        xml.append("    <DATA_CRIACAO>").append(pedido.getCriadoEm()).append("</DATA_CRIACAO>\n");
        xml.append("  </HEADER>\n");

        // Segments de itens
        xml.append("  <ITEMS>\n");
        int posicao = 1;
        for (var item : pedido.getItens()) {
            xml.append("    <ITEM>\n");
            xml.append("      <POSICAO>").append(posicao).append("</POSICAO>\n");
            xml.append("      <CODIGO_PRODUTO>").append(item.getCodigoProduto()).append("</CODIGO_PRODUTO>\n");
            xml.append("      <DESCRICAO>").append(item.getDescricao()).append("</DESCRICAO>\n");
            xml.append("      <QUANTIDADE>").append(item.getQuantidade()).append("</QUANTIDADE>\n");
            xml.append("      <PRECO_UNITARIO>").append(item.getPrecoUnitario()).append("</PRECO_UNITARIO>\n");
            xml.append("      <VALOR_TOTAL>").append(item.calcularValorTotal()).append("</VALOR_TOTAL>\n");
            xml.append("    </ITEM>\n");
            posicao++;
        }
        xml.append("  </ITEMS>\n");

        xml.append("</IDOC>\n");

        return xml.toString();
    }

    /**
     * Envia iDoc para fila de mensagens (simulado)
     * Em produção: usar Apache Kafka, RabbitMQ, AWS SQS, etc.
     */
    private void enviarParaFila(String idocId, String idocXml) throws IdocException {
        logger.debug("Enviando iDoc para fila. ID: {}", idocId);

        try {
            // Em produção:
            // kafkaTemplate.send("sap-orders-topic", idocXml);
            // ou
            // rabbitTemplate.convertAndSend("sap.orders.exchange", idocXml);

            // Para desenvolvimento: apenas log
            logger.info("iDoc enfileirado para processamento: {} bytes", idocXml.length());

        } catch (Exception e) {
            throw new IdocException("Falha ao enviar iDoc para fila: " + e.getMessage(), e);
        }
    }

    /**
     * iDoc Exception
     */
    public static class IdocException extends Exception {
        public IdocException(String message) {
            super(message);
        }

        public IdocException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
