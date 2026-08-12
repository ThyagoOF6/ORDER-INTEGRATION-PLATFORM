package com.orderintegration.core.domain.order;

/**
 * Enum: StatusPedido
 * Estados válidos de um Pedido no seu ciclo de vida
 * Máquina de estados: CRIADO → VALIDADO → SINCRONIZANDO → (SINCRONIZADO | ERRO)
 */
public enum StatusPedido {
    CRIADO("Pedido criado"),
    VALIDADO("Pedido validado"),
    SINCRONIZANDO("Sincronização com SAP em progresso"),
    SINCRONIZADO("Sincronização concluída com sucesso"),
    ERRO("Erro na sincronização");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
