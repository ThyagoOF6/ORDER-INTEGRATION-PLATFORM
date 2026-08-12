package com.orderintegration.core.domain.order;

import com.orderintegration.core.domain.common.DomainEvent;
import com.orderintegration.core.domain.order.events.PedidoCriadoEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Agregado Raiz: Pedido
 * Representa um pedido de cliente com seus itens e gerencia o estado através da
 * máquina de estados.
 * Implementa padrão de Agregado do Domain-Driven Design.
 */
public class Pedido {

    private final PedidoId pedidoId;
    private final String codigoCliente;
    private StatusPedido status;
    private final List<ItemPedido> itens;
    private BigDecimal valorTotal;
    private final Instant criadoEm;
    private String mensagemErro;
    private final List<DomainEvent> eventos;

    // Construtor privado para encapsulamento
    private Pedido(PedidoId pedidoId, String codigoCliente, List<ItemPedido> itens) {
        this.pedidoId = Objects.requireNonNull(pedidoId, "PedidoId não pode ser nulo");
        this.codigoCliente = Objects.requireNonNull(codigoCliente, "Código do cliente não pode ser nulo");
        this.itens = Objects.requireNonNull(itens, "Itens não pode ser nulo");
        this.status = StatusPedido.CRIADO;
        this.criadoEm = Instant.now();
        this.eventos = new ArrayList<>();

        calcularValorTotal();

        // Publica evento de criação
        this.eventos.add(new PedidoCriadoEvent(pedidoId, codigoCliente));
    }

    /**
     * Factory method para criar um novo Pedido
     */
    public static Pedido criar(String codigoCliente, List<ItemPedido> itens) {
        validarPrecondições(codigoCliente, itens);
        return new Pedido(PedidoId.gerar(), codigoCliente, new ArrayList<>(itens));
    }

    /**
     * Factory method para reconstituir Pedido (usado na recuperação do banco)
     */
    public static Pedido reconstituit(String pedidoId, String codigoCliente, StatusPedido status,
            List<ItemPedido> itens, BigDecimal valorTotal,
            Instant criadoEm, String mensagemErro) {
        var pedido = new Pedido(PedidoId.de(pedidoId), codigoCliente, itens);
        pedido.status = status;
        pedido.valorTotal = valorTotal;
        pedido.mensagemErro = mensagemErro;
        // Não publica eventos na reconstituição (o evento já foi publicado
        // anteriormente)
        pedido.eventos.clear();
        return pedido;
    }

    /**
     * Valida o pedido - transição de CRIADO → VALIDADO
     */
    public void validar() {
        if (status != StatusPedido.CRIADO) {
            throw new IllegalStateException(
                    String.format("Pedido em estado %s não pode ser validado. Estado esperado: %s",
                            status, StatusPedido.CRIADO));
        }

        if (itens.isEmpty()) {
            throw new IllegalStateException("Pedido sem itens não pode ser validado");
        }

        this.status = StatusPedido.VALIDADO;
    }

    /**
     * Inicia sincronização com SAP - transição de VALIDADO → SINCRONIZANDO
     */
    public void iniciarSincronizacao() {
        if (status != StatusPedido.VALIDADO) {
            throw new IllegalStateException(
                    String.format("Pedido em estado %s não pode iniciar sincronização. Estado esperado: %s",
                            status, StatusPedido.VALIDADO));
        }
        this.status = StatusPedido.SINCRONIZANDO;
        this.mensagemErro = null;
    }

    /**
     * Confirma sincronização bem-sucedida - transição de SINCRONIZANDO →
     * SINCRONIZADO
     */
    public void confirmarSincronizacao() {
        if (status != StatusPedido.SINCRONIZANDO) {
            throw new IllegalStateException(
                    String.format("Pedido em estado %s não pode confirmar sincronização. Estado esperado: %s",
                            status, StatusPedido.SINCRONIZANDO));
        }
        this.status = StatusPedido.SINCRONIZADO;
        this.mensagemErro = null;
    }

    /**
     * Registra erro de sincronização - transição de SINCRONIZANDO → ERRO
     */
    public void registrarErro(String mensagem) {
        if (status != StatusPedido.SINCRONIZANDO) {
            throw new IllegalStateException(
                    String.format("Pedido em estado %s não pode registrar erro. Estado esperado: %s",
                            status, StatusPedido.SINCRONIZANDO));
        }
        this.status = StatusPedido.ERRO;
        this.mensagemErro = Objects.requireNonNull(mensagem, "Mensagem de erro não pode ser nula");
    }

    /**
     * Calcula o valor total do pedido
     */
    private void calcularValorTotal() {
        this.valorTotal = itens.stream()
                .map(ItemPedido::calcularValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Limpa os eventos após serem persistidos
     */
    public void limparEventos() {
        this.eventos.clear();
    }

    // Validações privadas
    private static void validarPrecondições(String codigoCliente, List<ItemPedido> itens) {
        if (codigoCliente == null || codigoCliente.isBlank()) {
            throw new IllegalArgumentException("Código do cliente não pode estar vazio");
        }
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Pedido deve ter pelo menos um item");
        }
    }

    // Getters para acesso (manter imutabilidade)
    public String getPedidoId() {
        return pedidoId.getValor();
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public List<ItemPedido> getItens() {
        return new ArrayList<>(itens);
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public List<DomainEvent> getEventos() {
        return new ArrayList<>(eventos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(pedidoId, pedido.pedidoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pedidoId);
    }
}
