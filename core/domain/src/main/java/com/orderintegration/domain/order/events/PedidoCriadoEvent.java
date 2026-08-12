package com.orderintegration.domain.order.events;

import com.orderintegration.domain.common.DomainEvent;
import com.orderintegration.domain.order.PedidoId;

import java.util.Objects;

/**
 * Domain Event: Pedido foi criado.
 * 
 * Publicado quando um novo pedido entra no sistema.
 */
public class PedidoCriadoEvent extends DomainEvent {

    private final PedidoId pedidoId;
    private final String codigoCliente;

    public PedidoCriadoEvent(PedidoId pedidoId, String codigoCliente) {
        super("PedidoCriado");
        this.pedidoId = Objects.requireNonNull(pedidoId, "PedidoId é obrigatório");
        this.codigoCliente = Objects.requireNonNull(codigoCliente, "Código do cliente é obrigatório");
    }

    public PedidoId pedidoId() {
        return pedidoId;
    }

    public String codigoCliente() {
        return codigoCliente;
    }

    @Override
    public String toString() {
        return "PedidoCriadoEvent{" +
                "pedidoId=" + pedidoId +
                ", codigoCliente='" + codigoCliente + '\'' +
                ", ocorridoEm=" + ocorridoEm() +
                '}';
    }
}
