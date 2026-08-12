package com.orderintegration.core.domain.order.events;

import com.orderintegration.core.domain.common.DomainEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain Event: Pedido sincronizado com sucesso em SAP
 * Publicado após confirmação de sincronização
 * Subscribers podem: registrar auditoria, enviar notificações, atualizar cache,
 * etc.
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

    @Override
    public String getAggregateId() {
        return pedidoId;
    }

    @Override
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pedidoId", pedidoId);
        payload.put("transacaoSapId", transacaoSapId);
        payload.put("statusSap", statusSap);
        return payload;
    }
}
