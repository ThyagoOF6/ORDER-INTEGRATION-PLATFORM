package com.orderintegration.application.port;

import com.orderintegration.core.domain.order.Pedido;

/**
 * Port para sincronização com SAP
 * Hexagonal Architecture: Interface que define contrato com sistema externo
 * Implementação: adapter/out/messaging/SapOrderAdapter.java
 */
public interface SapSyncPort {
    
    /**
     * Sincroniza um pedido com o sistema SAP via RFC
     * @param pedido o pedido a sincronizar
     * @return ID da transação SAP
     * @throws SapSyncException se a sincronização falhar
     */
    String sincronizarPedidoRfc(Pedido pedido);
    
    /**
     * Publica um iDoc para o sistema SAP
     * Integração assíncrona via middleware de mensagens
     * @param pedido o pedido a publicar como iDoc
     * @return ID do iDoc gerado
     * @throws SapSyncException se a publicação falhar
     */
    String publicarPedidoIdoc(Pedido pedido);
    
    /**
     * Exceção de sincronização com SAP
     */
    class SapSyncException extends RuntimeException {
        public SapSyncException(String message) {
            super(message);
        }
        
        public SapSyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
