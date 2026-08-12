package com.orderintegration.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes do Value Object ItemPedido")
class ItemPedidoTest {

    @Test
    @DisplayName("Deve criar item com dados válidos")
    void deveCriarItemComDadosValidos() {
        // Act
        ItemPedido item = ItemPedido.criar("PROD-001", "Produto A", 5, new BigDecimal("25.00"));

        // Assert
        assertThat(item.codigoProduto()).isEqualTo("PROD-001");
        assertThat(item.descricao()).isEqualTo("Produto A");
        assertThat(item.quantidade()).isEqualTo(5);
        assertThat(item.precoUnitario()).isEqualTo(new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("Deve calcular valor total corretamente")
    void deveCalcularValorTotalCorreto() {
        // Arrange
        ItemPedido item = ItemPedido.criar("PROD-001", "Produto A", 10, new BigDecimal("15.50"));

        // Act
        BigDecimal valorTotal = item.calcularValorTotal();

        // Assert
        assertThat(valorTotal).isEqualTo(new BigDecimal("155.00"));
    }

    @Test
    @DisplayName("Deve lançar exceção se quantidade for zero")
    void deveLancarExcecaoSeQuantidadeForZero() {
        // Act & Assert
        assertThatThrownBy(() -> ItemPedido.criar("PROD-001", "Produto A", 0, new BigDecimal("25.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade deve ser maior que zero");
    }

    @Test
    @DisplayName("Deve lançar exceção se quantidade for negativa")
    void deveLancarExcecaoSeQuantidadeForNegativa() {
        // Act & Assert
        assertThatThrownBy(() -> ItemPedido.criar("PROD-001", "Produto A", -5, new BigDecimal("25.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade deve ser maior que zero");
    }

    @Test
    @DisplayName("Deve lançar exceção se preço for negativo")
    void deveLancarExcecaoSePrecoForNegativo() {
        // Act & Assert
        assertThatThrownBy(() -> ItemPedido.criar("PROD-001", "Produto A", 5, new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Preço não pode ser negativo");
    }

    @Test
    @DisplayName("Deve ter igualdade baseada em valor")
    void deveTerigualdadeBaseadaEmValor() {
        // Arrange
        ItemPedido item1 = ItemPedido.criar("PROD-001", "Produto A", 5, new BigDecimal("25.00"));
        ItemPedido item2 = ItemPedido.criar("PROD-001", "Produto A", 5, new BigDecimal("25.00"));
        ItemPedido item3 = ItemPedido.criar("PROD-002", "Produto B", 5, new BigDecimal("25.00"));

        // Assert
        assertThat(item1).isEqualTo(item2);
        assertThat(item1).isNotEqualTo(item3);
    }
}
