package com.orderintegration.core.domain.order;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: PedidoId
 * Identificador único e imutável de um Pedido
 */
public final class PedidoId {

    private final String valor;

    private PedidoId(String valor) {
        this.valor = Objects.requireNonNull(valor, "Valor do PedidoId não pode ser nulo");
    }

    /**
     * Gera um novo PedidoId com UUID
     */
    public static PedidoId gerar() {
        return new PedidoId(UUID.randomUUID().toString());
    }

    /**
     * Cria um PedidoId a partir de uma string
     */
    public static PedidoId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("PedidoId não pode ser vazio");
        }
        try {
            UUID.fromString(valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("PedidoId '%s' não é um UUID válido", valor), e);
        }
        return new PedidoId(valor);
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PedidoId pedidoId = (PedidoId) o;
        return Objects.equals(valor, pedidoId.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
