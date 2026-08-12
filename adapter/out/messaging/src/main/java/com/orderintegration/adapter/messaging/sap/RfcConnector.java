package com.orderintegration.adapter.messaging.sap;

import com.orderintegration.core.domain.order.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.time.Instant;
import java.util.UUID;

/**
 * RFC Connector: Comunicação síncrona com SAP via RFC (Remote Function Call)
 * Responsável por: criar pedidos, atualizar status, consultar dados em SAP
 * 
 * Nota: Implementação simulada para desenvolvimento
 * Em produção, usar: com.sap.conn.jco.* (SAP JCo library)
 */
@Component
public class RfcConnector {
    
    private static final Logger logger = LoggerFactory.getLogger(RfcConnector.class);
    
    private final SapConnectorConfig config;
    
    public RfcConnector(SapConnectorConfig config) {
        this.config = config;
    }
    
    /**
     * Chama RFC ZORDERS_CREATE para criar pedido em SAP
     * Integração síncrona com timeout e retry automático
     * 
     * @param pedido o pedido a criar
     * @return ID da transação SAP (ex: "SAP-2024-001234")
     * @throws RfcException em caso de falha
     */
    @Retryable(
        retryFor = RfcException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000)
    )
    public String criarPedidoRfc(Pedido pedido) throws RfcException {
        logger.info("Iniciando RFC para criar pedido: {}", pedido.getPedidoId());
        
        try {
            // Simulate RFC connection and call
            // Em produção: usar SAP JCo para chamar ZORDERS_CREATE
            
            RfcRequest request = montarRequestCriacaoPedido(pedido);
            RfcResponse response = chamarRfcComTimeout(
                config.getFunctionModuleCreateOrder(), 
                request,
                config.getConnectionTimeoutMs()
            );
            
            if (!response.isSuccess()) {
                throw new RfcException(
                    String.format("Erro ao criar pedido em SAP: %s (%s)",
                        response.getMessage(), response.getErrorCode())
                );
            }
            
            String transacaoId = response.getTransactionId();
            logger.info("Pedido criado em SAP com sucesso. Transação: {}", transacaoId);
            
            return transacaoId;
            
        } catch (Exception e) {
            logger.error("Falha na chamada RFC ZORDERS_CREATE para pedido: {}", 
                pedido.getPedidoId(), e);
            throw new RfcException(
                String.format("Falha ao criar pedido em SAP: %s", e.getMessage()), 
                e
            );
        }
    }
    
    /**
     * Chama RFC ZORDERS_UPDATE para atualizar status do pedido em SAP
     */
    @Retryable(
        retryFor = RfcException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000)
    )
    public String atualizarStatusPedidoRfc(String pedidoId, String novoStatus) throws RfcException {
        logger.info("Atualizando status do pedido em SAP: {} → {}", pedidoId, novoStatus);
        
        try {
            RfcRequest request = new RfcRequest();
            request.addParameter("PEDIDO_ID", pedidoId);
            request.addParameter("STATUS_NOVO", novoStatus);
            
            RfcResponse response = chamarRfcComTimeout(
                config.getFunctionModuleUpdateOrder(),
                request,
                config.getConnectionTimeoutMs()
            );
            
            if (!response.isSuccess()) {
                throw new RfcException(
                    String.format("Erro ao atualizar pedido em SAP: %s", response.getMessage())
                );
            }
            
            logger.info("Status do pedido atualizado em SAP: {}", pedidoId);
            return response.getTransactionId();
            
        } catch (Exception e) {
            logger.error("Falha ao atualizar pedido em SAP: {}", pedidoId, e);
            throw new RfcException("Falha ao atualizar pedido em SAP: " + e.getMessage(), e);
        }
    }
    
    /**
     * Monta request para RFC ZORDERS_CREATE
     */
    private RfcRequest montarRequestCriacaoPedido(Pedido pedido) {
        RfcRequest request = new RfcRequest();
        
        // Dados do pedido
        request.addParameter("CLIENTE_ID", pedido.getCodigoCliente());
        request.addParameter("PEDIDO_ID", pedido.getPedidoId());
        request.addParameter("STATUS", pedido.getStatus().name());
        request.addParameter("VALOR_TOTAL", pedido.getValorTotal().toString());
        request.addParameter("DATA_CRIACAO", pedido.getCriadoEm().toString());
        
        // Itens do pedido
        int contador = 0;
        for (var item : pedido.getItens()) {
            contador++;
            request.addParameter("ITEM_" + contador + "_CODIGO", item.getCodigoProduto());
            request.addParameter("ITEM_" + contador + "_DESCRICAO", item.getDescricao());
            request.addParameter("ITEM_" + contador + "_QTD", item.getQuantidade().toString());
            request.addParameter("ITEM_" + contador + "_PRECO", item.getPrecoUnitario().toString());
        }
        request.addParameter("TOTAL_ITENS", String.valueOf(contador));
        
        return request;
    }
    
    /**
     * Executa chamada RFC com timeout
     * Implementação simulada - em produção usar JCo real
     */
    private RfcResponse chamarRfcComTimeout(String moduleName, RfcRequest request, 
                                            Long timeoutMs) throws RfcException {
        logger.debug("Chamando RFC: {}", moduleName);
        
        // Simular chamada RFC (em produção: usar com.sap.conn.jco.JCo)
        // Este é um mock para ambiente de desenvolvimento
        
        try {
            // Simula latência de rede/processamento
            Thread.sleep(Math.min(500, timeoutMs));
            
            // Em desenvolvimento: sempre sucesso
            RfcResponse response = new RfcResponse();
            response.setSuccess(true);
            response.setTransactionId("SAP-" + System.currentTimeMillis());
            response.setMessage("Operação concluída com sucesso");
            
            return response;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RfcException("Timeout na chamada RFC: " + moduleName, e);
        }
    }
    
    /**
     * Inner class: RFC Request
     */
    public static class RfcRequest {
        private java.util.Map<String, String> parameters = new java.util.HashMap<>();
        
        public void addParameter(String key, String value) {
            parameters.put(key, value);
        }
        
        public String getParameter(String key) {
            return parameters.get(key);
        }
        
        public java.util.Map<String, String> getParameters() {
            return parameters;
        }
    }
    
    /**
     * Inner class: RFC Response
     */
    public static class RfcResponse {
        private boolean success;
        private String transactionId;
        private String message;
        private String errorCode;
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getTransactionId() {
            return transactionId;
        }
        
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getErrorCode() {
            return errorCode;
        }
        
        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
    }
    
    /**
     * RFC Exception
     */
    public static class RfcException extends Exception {
        public RfcException(String message) {
            super(message);
        }
        
        public RfcException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
