package com.orderintegration.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO para requisição de criação de pedido
 */
public record PedidoRequestDTO(
        @NotBlank(message = "Código do cliente é obrigatório") String codigoCliente,

        @NotEmpty(message = "Pedido deve ter pelo menos um item") @Valid List<ItemPedidoDTO> itens) {
}
