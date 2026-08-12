package com.orderintegration.domain.order;

import com.orderintegration.domain.common.DomainEvent;
import com.orderintegration.domain.order.events.PedidoCriadoEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Aggregate Root: Pedido
 * 
 * Padrão: DDD - Aggregate Root
 * Responsável por:
 * - Manter invariantes de pedido
 * - Orquestrar transições de estado
 * - Publicar eventos de domínio
 * 
 * Invariantes:
 * - Um pedido deve ter pelo menos um item
 * - Um pedido não pode estar vazio
 * - Transições de estado só podem acontecer em sequência válida
 */
public class Pedido {

    private final PedidoId id;
    private final String codigoCliente;
    private final List<ItemPedido> itens;
    private StatusPedido status;
    private final Instant criadoEm;
    private Instant atualizadoEm;
    private final List<DomainEvent> eventos;

    private Pedido(PedidoId id, String codigoCliente, List<ItemPedido> itens, StatusPedido status,
            Instant criadoEm, Instant atualizadoEm) {
        this.id = Objects.requireNonNull(id, "PedidoId é obrigatório");
        this.codigoCliente = Objects.requireNonNull(codigoCliente, "Código do cliente é obrigatório");
        this.itens = new ArrayList<>(Objects.requireNonNull(itens, "Itens é obrigatório"));
        this.status = Objects.requireNonNull(status, "Status é obrigatório");
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm é obrigatório");
        this.atualizadoEm = Objects.requireNonNull(atualizadoEm, "atualizadoEm é obrigatório");
        this.eventos = new ArrayList<>();
    }

    /**
     * Factory Method: Criar novo pedido
     */
    public static Pedido criar(String codigoCliente, List<ItemPedido> itens) {
        if (codigoCliente == null || codigoCliente.isBlank()) {
            throw new IllegalArgumentException("Código do cliente é obrigatório");
        }
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Pedido deve ter pelo menos um item");
        }

        PedidoId id = PedidoId.gerar();
        Instant agora = Instant.now();
        Pedido pedido = new Pedido(id, codigoCliente, itens, StatusPedido.CRIADO, agora, agora);

        // Publicar evento de criação
        pedido.eventos.add(new PedidoCriadoEvent(id, codigoCliente));

        return pedido;
    }

    /**
     * Reconstrói um pedido a partir do banco (sem publicar eventos)
     */
    public static Pedido reconstituir(PedidoId id, String codigoCliente, List<ItemPedido> itens,
            StatusPedido status, Instant criadoEm, Instant atualizadoEm) {
        return new Pedido(id, codigoCliente, itens, status, criadoEm, atualizadoEm);
    }

    /**
     * Validar pedido segundo regras de negócio
     */
    public void validar() {
        if (itens.isEmpty()) {
            throw new IllegalStateException("Pedido deve conter itens");
        }
        // Adicionar mais validações de negócio conforme necessário
        this.status = StatusPedido.VALIDADO;
        this.atualizadoEm = Instant.now();
    }

    /**
     * Iniciar sincronização com ERP
     */
    public void iniciarSincronizacao() {
        if (status != StatusPedido.VALIDADO) {
            throw new IllegalStateException("Apenas pedidos validados podem ser sincronizados");
        }
        this.status = StatusPedido.SINCRONIZANDO;
        this.atualizadoEm = Instant.now();
    }

    /**
     * Confirmar sincronização com sucesso
     */
    public void confirmarSincronizacao() {
        if (status != StatusPedido.SINCRONIZANDO) {
            throw new IllegalStateException("Pedido não está em sincronização");
        }
        this.status = StatusPedido.SINCRONIZADO;
        this.atualizadoEm = Instant.now();
    }

    /**
     * Registrar erro na sincronização
     */
    public void registrarErro() {
        this.status = StatusPedido.ERRO;
        this.atualizadoEm = Instant.now();
    }

    // Getters
    public PedidoId id() {
        return id;
    }

    public String codigoCliente() {
        return codigoCliente;
    }

    public List<ItemPedido> itens() {
        return Collections.unmodifiableList(itens);
    }

    public StatusPedido status() {
        return status;
    }

    public Instant criadoEm() {
        return criadoEm;
    }

    public Instant atualizadoEm() {
        return atualizadoEm;
    }

    public List<DomainEvent> eventos() {
        return Collections.unmodifiableList(eventos);
    }

    /**
     * Limpar eventos após persistência
     */
    public void limparEventos() {
        eventos.clear();
    }

    /**
     * Calcular valor total do pedido
     */
    public BigDecimal calcularValorTotal() {
        return itens.stream()
                .map(ItemPedido::calcularValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", codigoCliente='" + codigoCliente + '\'' +
                ", status=" + status +
                ", criadoEm=" + criadoEm +
                '}';
    }
}
