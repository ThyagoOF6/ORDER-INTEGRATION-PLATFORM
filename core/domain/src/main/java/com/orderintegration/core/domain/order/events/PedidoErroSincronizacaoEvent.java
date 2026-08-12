package com.orderintegration.core.domain.order.events;

import com.orderintegration.core.domain.common.DomainEvent;

/**
 * Domain Event: Erro na sincronização com SAP
 * Publicado quando sincronização falha
 * Subscribers podem: alertas, retry logic, escalação, rollback, etc.
 */
public class PedidoErroSincronizacaoEvent extends DomainEvent {
    
    private final String pedidoId;
    private final String codigoErro;
    private final String mensagemErro;
    private final String tentativa;
    
    public PedidoErroSincronizacaoEvent(String pedidoId, String codigoErro, 
                                       String mensagemErro, String tentativa) {
        super();
        this.pedidoId = pedidoId;
        this.codigoErro = codigoErro;
        this.mensagemErro = mensagemErro;
        this.tentativa = tentativa;
    }
    
    public String getPedidoId() {
        return pedidoId;
    }
    
    public String getCodigoErro() {
        return codigoErro;
    }
    
    public String getMensagemErro() {
        return mensagemErro;
    }
    
    public String getTentativa() {
        return tentativa;
    }
}
