package com.orderintegration.application.dto;

import java.math.BigDecimal;

/**
 * DTO para item de pedido em requisições e respostas
 */
public record ItemPedidoDTO(
        String codigoProduto,
        String descricao,
        Integer quantidade,
        BigDecimal precoUnitario) {
}
