package com.orderintegration.core.domain.order;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: ItemPedido
 * Representa um item de linha do pedido com imutabilidade
 */
public final class ItemPedido {

    private final String codigoProduto;
    private final String descricao;
    private final Integer quantidade;
    private final BigDecimal precoUnitario;

    private ItemPedido(String codigoProduto, String descricao, Integer quantidade, BigDecimal precoUnitario) {
        this.codigoProduto = Objects.requireNonNull(codigoProduto, "Código do produto não pode ser nulo");
        this.descricao = Objects.requireNonNull(descricao, "Descrição não pode ser nula");
        this.quantidade = Objects.requireNonNull(quantidade, "Quantidade não pode ser nula");
        this.precoUnitario = Objects.requireNonNull(precoUnitario, "Preço unitário não pode ser nulo");
    }

    /**
     * Factory method para criar um novo ItemPedido
     */
    public static ItemPedido criar(String codigoProduto, String descricao, Integer quantidade,
            BigDecimal precoUnitario) {
        validarPrecondições(quantidade, precoUnitario);
        return new ItemPedido(codigoProduto, descricao, quantidade, precoUnitario);
    }

    /**
     * Calcula o valor total do item (quantidade × preço unitário)
     */
    public BigDecimal calcularValorTotal() {
        return precoUnitario.multiply(new BigDecimal(quantidade));
    }

    private static void validarPrecondições(Integer quantidade, BigDecimal precoUnitario) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (precoUnitario == null || precoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço unitário não pode ser negativo");
        }
    }

    // Getters
    public String getCodigoProduto() {
        return codigoProduto;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ItemPedido that = (ItemPedido) o;
        return Objects.equals(codigoProduto, that.codigoProduto) &&
                Objects.equals(descricao, that.descricao) &&
                Objects.equals(quantidade, that.quantidade) &&
                Objects.equals(precoUnitario, that.precoUnitario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoProduto, descricao, quantidade, precoUnitario);
    }

    @Override
    public String toString() {
        return String.format("ItemPedido{cod=%s, desc=%s, qty=%d, preco=%.2f}",
                codigoProduto, descricao, quantidade, precoUnitario);
    }
}
