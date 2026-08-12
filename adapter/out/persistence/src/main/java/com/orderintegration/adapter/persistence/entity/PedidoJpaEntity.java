package com.orderintegration.adapter.persistence.entity;

import com.orderintegration.core.domain.order.ItemPedido;
import com.orderintegration.core.domain.order.Pedido;
import com.orderintegration.core.domain.order.PedidoId;
import com.orderintegration.core.domain.order.StatusPedido;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity JPA para Pedido (Aggregate Root)
 */
@Entity
@Table(name = "pedido")
public class PedidoJpaEntity {

    @Id
    @Column(name = "pedido_id", length = 36)
    private String pedidoId;

    @Column(nullable = false, length = 50)
    private String codigoCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false, name = "criado_em")
    private Instant criadoEm;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemPedidoJpaEntity> itens = new ArrayList<>();

    @Column(name = "mensagem_erro", length = 500)
    private String mensagemErro;

    // Construtores

    public PedidoJpaEntity() {
    }

    public PedidoJpaEntity(Pedido pedido) {
        this.pedidoId = pedido.getPedidoId();
        this.codigoCliente = pedido.getCodigoCliente();
        this.status = pedido.getStatus();
        this.valorTotal = pedido.getValorTotal();
        this.criadoEm = pedido.getCriadoEm();
        this.itens = pedido.getItens().stream()
                .map(ItemPedidoJpaEntity::new)
                .toList()
                .stream()
                .peek(item -> item.setPedido(this))
                .toList();
    }

    // Getters e Setters

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<ItemPedidoJpaEntity> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoJpaEntity> itens) {
        this.itens = itens;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    // Converter para domínio

    public Pedido toDomain() {
        List<ItemPedido> itensDominio = this.itens.stream()
                .map(ItemPedidoJpaEntity::toDomain)
                .toList();

        return Pedido.reconstituit(
                this.pedidoId,
                this.codigoCliente,
                this.status,
                itensDominio,
                this.valorTotal,
                this.criadoEm,
                this.mensagemErro);
    }
}
