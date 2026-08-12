package com.orderintegration.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes do Agregado Pedido")
class PedidoTest {

    @Test
    @DisplayName("Deve criar um novo pedido com status CRIADO")
    void deveCriarNovoPedidoComStatusCriado() {
        // Arrange
        String codigoCliente = "CLI-001";
        List<ItemPedido> itens = List.of(
                ItemPedido.criar("PROD-001", "Produto A", 2, new BigDecimal("50.00")));

        // Act
        Pedido pedido = Pedido.criar(codigoCliente, itens);

        // Assert
        assertThat(pedido.codigoCliente()).isEqualTo(codigoCliente);
        assertThat(pedido.status()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.itens()).hasSize(1);
        assertThat(pedido.eventos()).hasSize(1);
        assertThat(pedido.eventos().get(0).nomeEvento()).isEqualTo("PedidoCriado");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar pedido sem itens")
    void deveLancarExcecaoAoCriarPedidoSemItens() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> Pedido.criar("CLI-001", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pedido deve ter pelo menos um item");
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar pedido sem cliente")
    void deveLancarExcecaoAoCriarPedidoSemCliente() {
        // Arrange
        List<ItemPedido> itens = List.of(
                ItemPedido.criar("PROD-001", "Produto A", 1, new BigDecimal("50.00")));

        // Act & Assert
        assertThatThrownBy(() -> Pedido.criar("", itens))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve calcular valor total correto")
    void deveCalcularValorTotalCorreto() {
        // Arrange
        List<ItemPedido> itens = List.of(
                ItemPedido.criar("PROD-001", "Produto A", 2, new BigDecimal("50.00")),
                ItemPedido.criar("PROD-002", "Produto B", 3, new BigDecimal("30.00")));
        Pedido pedido = Pedido.criar("CLI-001", itens);

        // Act
        BigDecimal valorTotal = pedido.calcularValorTotal();

        // Assert
        assertThat(valorTotal).isEqualTo(new BigDecimal("190.00"));
    }

    @Test
    @DisplayName("Deve validar pedido e mudar status para VALIDADO")
    void deveValidarPedidoEMudarStatusParaValidado() {
        // Arrange
        List<ItemPedido> itens = List.of(
                ItemPedido.criar("PROD-001", "Produto A", 1, new BigDecimal("50.00")));
        Pedido pedido = Pedido.criar("CLI-001", itens);

        // Act
        pedido.validar();

        // Assert
        assertThat(pedido.status()).isEqualTo(StatusPedido.VALIDADO);
    }

    @Test
    @DisplayName("Deve iniciar sincronização apenas se pedido está validado")
    void deveIniciarSincronizacaoApenasSeValidado() {
        // Arrange
        List<ItemPedido> itens = List.of(
                ItemPedido.criar("PROD-001", "Produto A", 1, new BigDecimal("50.00")));
        Pedido pedido = Pedido.criar("CLI-001", itens);

        // Act & Assert
        assertThatThrownBy(pedido::iniciarSincronizacao)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Apenas pedidos validados podem ser sincronizados");

        // Validar e tentar novamente
        pedido.validar();
        pedido.iniciarSincronizacao();

        assertThat(pedido.status()).isEqualTo(StatusPedido.SINCRONIZANDO);
    }

    @Test
    @DisplayName("Deve confirmar sincronização com sucesso")
    void deveConfirmarSincronizacaoComSucesso() {
        // Arrange
        List<ItemPedido> itens = List.of(
                ItemPedido.criar("PROD-001", "Produto A", 1, new BigDecimal("50.00")));
        Pedido pedido = Pedido.criar("CLI-001", itens);
        pedido.validar();
        pedido.iniciarSincronizacao();

        // Act
        pedido.confirmarSincronizacao();

        // Assert
        assertThat(pedido.status()).isEqualTo(StatusPedido.SINCRONIZADO);
    }

    @Test
    @DisplayName("Deve registrar erro na sincronização")
    void deveRegistrarErroNaSincronizacao() {
        // Arrange
        List<ItemPedido> itens = List.of(
                ItemPedido.criar("PROD-001", "Produto A", 1, new BigDecimal("50.00")));
        Pedido pedido = Pedido.criar("CLI-001", itens);
        pedido.validar();
        pedido.iniciarSincronizacao();

        // Act
        pedido.registrarErro();

        // Assert
        assertThat(pedido.status()).isEqualTo(StatusPedido.ERRO);
    }

    @Test
    @DisplayName("Deve limpar eventos após consumo")
    void deveLimparEventosAposConsumo() {
        // Arrange
        List<ItemPedido> itens = List.of(
                ItemPedido.criar("PROD-001", "Produto A", 1, new BigDecimal("50.00")));
        Pedido pedido = Pedido.criar("CLI-001", itens);
        assertThat(pedido.eventos()).hasSize(1);

        // Act
        pedido.limparEventos();

        // Assert
        assertThat(pedido.eventos()).isEmpty();
    }
}
