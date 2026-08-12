package com.orderintegration.domain.order;

/**
 * Value Object: Estado do pedido.
 * 
 * Estados válidos:
 * - CRIADO: Acaba de ser criado
 * - VALIDADO: Passou por validações de negócio
 * - SINCRONIZANDO: Sendo enviado para ERP
 * - SINCRONIZADO: Confirmado no ERP
 * - ERRO: Falha na sincronização
 */
public enum StatusPedido {
    CRIADO,
    VALIDADO,
    SINCRONIZANDO,
    SINCRONIZADO,
    ERRO
}
