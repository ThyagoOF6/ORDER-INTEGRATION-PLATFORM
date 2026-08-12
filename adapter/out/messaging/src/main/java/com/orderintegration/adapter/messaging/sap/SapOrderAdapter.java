package com.orderintegration.adapter.messaging.sap;

import com.orderintegration.application.port.SapSyncPort;
import com.orderintegration.core.domain.order.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SAP Order Adapter: Implementação da porta SapSyncPort
 * Hexagonal Architecture: Adapter que implementa contrato definido pela application
 * 
 * Responsável por: orquestrar RFC para sync síncrono e iDoc para async
 */
@Component
public class SapOrderAdapter implements SapSyncPort {
    
    private static final Logger logger = LoggerFactory.getLogger(SapOrderAdapter.class);
    
    private final RfcConnector rfcConnector;
    private final IdocPublisher idocPublisher;
    
    public SapOrderAdapter(RfcConnector rfcConnector, IdocPublisher idocPublisher) {
        this.rfcConnector = rfcConnector;
        this.idocPublisher = idocPublisher;
    }
    
    @Override
    public String sincronizarPedidoRfc(Pedido pedido) {
        logger.info("Iniciando sincronização RFC do pedido: {}", pedido.getPedidoId());
        
        try {
            // Sincronização síncrona com SAP via RFC
            String transacaoId = rfcConnector.criarPedidoRfc(pedido);
            
            logger.info("Pedido sincronizado com RFC. Transação: {}", transacaoId);
            return transacaoId;
            
        } catch (RfcConnector.RfcException e) {
            logger.error("Falha na sincronização RFC: {}", e.getMessage());
            throw new SapSyncException(
                String.format("Falha ao sincronizar pedido com SAP via RFC: %s", e.getMessage()), 
                e
            );
        }
    }
    
    @Override
    public String publicarPedidoIdoc(Pedido pedido) {
        logger.info("Iniciando publicação iDoc do pedido: {}", pedido.getPedidoId());
        
        try {
            // Publicação assíncrona para fila via iDoc
            String idocId = idocPublisher.publicarPedidoIdoc(pedido);
            
            logger.info("Pedido publicado como iDoc. ID: {}", idocId);
            return idocId;
            
        } catch (IdocPublisher.IdocException e) {
            logger.error("Falha na publicação iDoc: {}", e.getMessage());
            throw new SapSyncException(
                String.format("Falha ao publicar pedido como iDoc: %s", e.getMessage()), 
                e
            );
        }
    }
}
