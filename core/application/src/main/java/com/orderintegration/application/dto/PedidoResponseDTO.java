package com.orderintegration.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO para resposta de pedido
 */
public record PedidoResponseDTO(
        String pedidoId,
        String codigoCliente,
        String status,
        List<ItemPedidoDTO> itens,
        BigDecimal valorTotal,
        Instant criadoEm) {
}
