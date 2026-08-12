package com.orderintegration.domain.order;

import java.util.Objects;

/**
 * Value Object: Identificador único de um pedido.
 * 
 * Por que Value Object?
 * - Imutável por design
 * - Sem identidade própria, apenas valor
 * - Encapsula validação de formato
 */
public class PedidoId {

    private final String valor;

    private PedidoId(String valor) {
        this.valor = Objects.requireNonNull(valor, "PedidoId não pode ser nulo");
    }

    public static PedidoId gerar() {
        return new PedidoId(java.util.UUID.randomUUID().toString());
    }

    public static PedidoId de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("PedidoId não pode estar vazio");
        }
        return new PedidoId(valor);
    }

    public String valor() {
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
