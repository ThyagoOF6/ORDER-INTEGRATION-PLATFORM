package com.orderintegration.application.service;

import com.orderintegration.application.dto.IdocResponse;
import com.orderintegration.application.port.IdocResponsePort;
import com.orderintegration.application.port.PedidoRepositoryPort;
import com.orderintegration.core.domain.order.Pedido;
import com.orderintegration.core.domain.order.PedidoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de aplicação para processar respostas de iDoc da fila
 * 
 * Responsabilidades:
 * - Receber confirmações de iDoc do message broker
 * - Atualizar estado de pedidos (SINCRONIZANDO → SINCRONIZADO/ERRO)
 * - Registrar SAP message IDs para rastreabilidade
 * - Implementar retry logic para falhas transientes
 * 
 * Implementa: IdocResponsePort
 */
@Service
public class IdocResponseService implements IdocResponsePort {

    private static final Logger logger = LoggerFactory.getLogger(IdocResponseService.class);
    private final PedidoRepositoryPort pedidoRepository;
    private final EventPublisherService eventPublisherService;

    public IdocResponseService(PedidoRepositoryPort pedidoRepository, EventPublisherService eventPublisherService) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisherService = eventPublisherService;
    }

    /**
     * Processa resposta de iDoc bem-sucedida
     * SINCRONIZANDO → SINCRONIZADO
     */
    @Override
    @Transactional
    public void processarRespostaIDocSucesso(IdocResponse response) throws IdocResponseException {
        logger.info("Processando iDoc sucesso: idocId={}, pedidoId={}, sapMessageId={}",
                response.getIdocId(), response.getPedidoId(), response.getSapMessageId());

        try {
            // Buscar pedido pelo ID
            PedidoId pedidoId = PedidoId.de(response.getPedidoId());
            Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                    .orElseThrow(() -> new IdocResponseException(
                            "Pedido não encontrado: " + response.getPedidoId()));

            // Validar que está em SINCRONIZANDO
            if (!pedido.getStatus().name().equals("SINCRONIZANDO")) {
                logger.warn("Pedido não está em SINCRONIZANDO, status atual: {}", pedido.getStatus());
                throw new IdocResponseException(
                        "Pedido deve estar em SINCRONIZANDO para confirmar: " + pedido.getStatus());
            }

            // Confirmar sincronização (SINCRONIZANDO → SINCRONIZADO)
            pedido.confirmarSincronizacao();

            // Persistir
            pedidoRepository.atualizar(pedido);
            eventPublisherService.publicarEventos(pedido);

            logger.info("iDoc processado com sucesso para pedidoId={}, novo status={}",
                    pedidoId.getValor(), pedido.getStatus());

        } catch (IdocResponseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao processar resposta iDoc bem-sucedida", e);
            throw new IdocResponseException(
                    "Erro ao processar resposta iDoc: " + response.getIdocId(),
                    e);
        }
    }

    /**
     * Processa resposta de iDoc com erro
     * SINCRONIZANDO → ERRO
     */
    @Override
    @Transactional
    public void processarRespostaIdocErro(IdocResponse response) throws IdocResponseException {
        logger.warn("Processando iDoc erro: idocId={}, pedidoId={}, errorCode={}, errorMessage={}",
                response.getIdocId(), response.getPedidoId(),
                response.getErrorCode(), response.getErrorMessage());

        try {
            // Buscar pedido pelo ID
            PedidoId pedidoId = PedidoId.de(response.getPedidoId());
            Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                    .orElseThrow(() -> new IdocResponseException(
                            "Pedido não encontrado: " + response.getPedidoId()));

            // Validar que está em SINCRONIZANDO
            if (!pedido.getStatus().name().equals("SINCRONIZANDO")) {
                logger.warn("Pedido não está em SINCRONIZANDO, status atual: {}", pedido.getStatus());
                throw new IdocResponseException(
                        "Pedido deve estar em SINCRONIZANDO para registrar erro: " + pedido.getStatus());
            }

            // Registrar erro (SINCRONIZANDO → ERRO)
            String mensagemErro = String.format(
                    "iDoc Error [%s]: %s (SAP Error: %s)",
                    response.getErrorCode(),
                    response.getErrorMessage(),
                    response.getIdocId());
            pedido.registrarErro(mensagemErro);

            // Persistir
            pedidoRepository.atualizar(pedido);
            eventPublisherService.publicarEventos(pedido);

            logger.info("iDoc error registrado para pedidoId={}, novo status={}",
                    pedidoId.getValor(), pedido.getStatus());

        } catch (IdocResponseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao processar resposta iDoc com erro", e);
            throw new IdocResponseException(
                    "Erro ao processar resposta iDoc de erro: " + response.getIdocId(),
                    e);
        }
    }
}
