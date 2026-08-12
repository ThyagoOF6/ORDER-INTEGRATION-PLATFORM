package com.orderintegration.application.port;

import com.orderintegration.core.domain.order.Pedido;
import com.orderintegration.core.domain.order.PedidoId;
import java.util.Optional;

/**
 * Port (interface) para persistência de Pedidos
 * Implementação: adapter/out/persistence
 */
public interface PedidoRepositoryPort {

    /**
     * Salva um pedido
     */
    Pedido salvar(Pedido pedido);

    /**
     * Busca pedido por ID
     */
    Optional<Pedido> buscarPorId(PedidoId pedidoId);

    /**
     * Atualiza um pedido
     */
    Pedido atualizar(Pedido pedido);

    /**
     * Verifica se pedido existe
     */
    boolean existe(PedidoId pedidoId);
}
