package com.orderintegration.adapter.persistence.entity;

import com.orderintegration.core.domain.order.ItemPedido;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entity JPA para ItemPedido
 */
@Entity
@Table(name = "item_pedido")
public class ItemPedidoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpaEntity pedido;

    @Column(nullable = false, length = 50)
    private String codigoProduto;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    // Construtores

    public ItemPedidoJpaEntity() {
    }

    public ItemPedidoJpaEntity(ItemPedido itemPedido) {
        this.codigoProduto = itemPedido.getCodigoProduto();
        this.descricao = itemPedido.getDescricao();
        this.quantidade = itemPedido.getQuantidade();
        this.precoUnitario = itemPedido.getPrecoUnitario();
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PedidoJpaEntity getPedido() {
        return pedido;
    }

    public void setPedido(PedidoJpaEntity pedido) {
        this.pedido = pedido;
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public void setCodigoProduto(String codigoProduto) {
        this.codigoProduto = codigoProduto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    // Converter para domínio

    public ItemPedido toDomain() {
        return ItemPedido.criar(
                this.codigoProduto,
                this.descricao,
                this.quantidade,
                this.precoUnitario);
    }
}
