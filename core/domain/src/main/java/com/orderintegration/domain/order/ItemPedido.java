package com.orderintegration.domain.order;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Item do pedido.
 * 
 * Representa um produto no pedido com quantidade e preço unitário.
 * Imutável e encapsula lógica de cálculo de valor.
 */
public class ItemPedido {

    private final String codigoProduto;
    private final String descricao;
    private final Integer quantidade;
    private final BigDecimal precoUnitario;

    private ItemPedido(String codigoProduto, String descricao, Integer quantidade, BigDecimal precoUnitario) {
        this.codigoProduto = Objects.requireNonNull(codigoProduto, "Código do produto é obrigatório");
        this.descricao = Objects.requireNonNull(descricao, "Descrição é obrigatória");
        this.quantidade = Objects.requireNonNull(quantidade, "Quantidade é obrigatória");
        this.precoUnitario = Objects.requireNonNull(precoUnitario, "Preço unitário é obrigatório");

        validar();
    }

    public static ItemPedido criar(String codigoProduto, String descricao, Integer quantidade,
            BigDecimal precoUnitario) {
        return new ItemPedido(codigoProduto, descricao, quantidade, precoUnitario);
    }

    private void validar() {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (precoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
    }

    public BigDecimal calcularValorTotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public String codigoProduto() {
        return codigoProduto;
    }

    public String descricao() {
        return descricao;
    }

    public Integer quantidade() {
        return quantidade;
    }

    public BigDecimal precoUnitario() {
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
                Objects.equals(quantidade, that.quantidade) &&
                Objects.equals(precoUnitario, that.precoUnitario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoProduto, quantidade, precoUnitario);
    }

    @Override
    public String toString() {
        return "ItemPedido{" +
                "codigoProduto='" + codigoProduto + '\'' +
                ", quantidade=" + quantidade +
                ", precoUnitario=" + precoUnitario +
                '}';
    }
}
