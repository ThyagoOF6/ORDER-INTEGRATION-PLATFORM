package com.orderintegration.core.domain.order.events;

import com.orderintegration.core.domain.common.DomainEvent;
import com.orderintegration.core.domain.order.PedidoId;

import java.util.Map;

/**
 * Domain Event: PedidoCriadoEvent
 * Publicado quando um novo Pedido é criado
 * Pode ser subscrito por handlers para realizar ações secundárias (logging,
 * auditoria, etc.)
 */
public class PedidoCriadoEvent extends DomainEvent {

    private final String pedidoId;
    private final String codigoCliente;

    public PedidoCriadoEvent(PedidoId pedidoId, String codigoCliente) {
        super();
        this.pedidoId = pedidoId.getValor();
        this.codigoCliente = codigoCliente;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    @Override
    public String getAggregateId() {
        return pedidoId;
    }

    @Override
    public Map<String, Object> toPayload() {
        return Map.of(
                "pedidoId", pedidoId,
                "codigoCliente", codigoCliente);
    }
}
