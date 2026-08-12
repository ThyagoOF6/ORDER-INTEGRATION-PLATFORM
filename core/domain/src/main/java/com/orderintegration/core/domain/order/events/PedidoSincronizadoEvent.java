package com.orderintegration.core.domain.order.events;

import com.orderintegration.core.domain.common.DomainEvent;

/**
 * Domain Event: Pedido sincronizado com sucesso em SAP
 * Publicado após confirmação de sincronização
 * Subscribers podem: registrar auditoria, enviar notificações, atualizar cache, etc.
 */
public class PedidoSincronizadoEvent extends DomainEvent {
    
    private final String pedidoId;
    private final String transacaoSapId;
    private final String statusSap;
    
    public PedidoSincronizadoEvent(String pedidoId, String transacaoSapId, String statusSap) {
        super();
        this.pedidoId = pedidoId;
        this.transacaoSapId = transacaoSapId;
        this.statusSap = statusSap;
    }
    
    public String getPedidoId() {
        return pedidoId;
    }
    
    public String getTransacaoSapId() {
        return transacaoSapId;
    }
    
    public String getStatusSap() {
        return statusSap;
    }
}
