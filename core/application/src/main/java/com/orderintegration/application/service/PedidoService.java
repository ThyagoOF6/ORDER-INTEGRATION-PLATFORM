package com.orderintegration.application.service;

import com.orderintegration.application.dto.ItemPedidoDTO;
import com.orderintegration.application.dto.PedidoRequestDTO;
import com.orderintegration.application.dto.PedidoResponseDTO;
import com.orderintegration.application.port.PedidoRepositoryPort;
import com.orderintegration.application.port.SapSyncPort;
import com.orderintegration.core.domain.order.ItemPedido;
import com.orderintegration.core.domain.order.Pedido;
import com.orderintegration.core.domain.order.PedidoId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de aplicação para casos de uso do Pedido
 * Orquestra entre Application layer e Domain layer
 * Integra com SAP via SapSyncPort (RFC para sync, iDoc para async)
 */
@Service
public class PedidoService {
    
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepositoryPort pedidoRepository;
    private final SapSyncPort sapSyncPort;

    public PedidoService(PedidoRepositoryPort pedidoRepository, SapSyncPort sapSyncPort) {
        this.pedidoRepository = pedidoRepository;
        this.sapSyncPort = sapSyncPort;
    }

    /**
     * Caso de uso: Criar novo pedido
     */
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO request) {
        // Converter DTOs em domínio
        List<ItemPedido> itens = request.itens().stream()
                .map(dto -> ItemPedido.criar(
                        dto.codigoProduto(),
                        dto.descricao(),
                        dto.quantidade(),
                        dto.precoUnitario()))
                .toList();

        // Criar agregado usando factory
        Pedido pedido = Pedido.criar(request.codigoCliente(), itens);

        // Persistir
        Pedido pedidoSalvo = pedidoRepository.salvar(pedido);

        // Converter para response
        return converterParaResponse(pedidoSalvo);
    }

    /**
     * Caso de uso: Buscar pedido por ID
     */
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(String pedidoId) {
        Pedido pedido = pedidoRepository.buscarPorId(PedidoId.de(pedidoId))
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));

        return converterParaResponse(pedido);
    }

    /**
     * Caso de uso: Validar pedido
     */
    @Transactional
    public PedidoResponseDTO validarPedido(String pedidoId) {
        Pedido pedido = pedidoRepository.buscarPorId(PedidoId.de(pedidoId))
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));

        pedido.validar();
        Pedido pedidoAtualizado = pedidoRepository.atualizar(pedido);

        return converterParaResponse(pedidoAtualizado);
    }

    /**
     * Caso de uso: Iniciar sincronização com SAP
     */
    @Transactional
    public PedidoResponseDTO iniciarSincronizacao(String pedidoId) {
        Pedido pedido = pedidoRepository.buscarPorId(PedidoId.de(pedidoId))
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));

        pedido.iniciarSincronizacao();
        Pedido pedidoAtualizado = pedidoRepository.atualizar(pedido);

        return converterParaResponse(pedidoAtualizado);
    }

    /**
     * Caso de uso: Confirmar sincronização bem-sucedida
     */
    @Transactional
    public PedidoResponseDTO confirmarSincronizacao(String pedidoId) {
        Pedido pedido = pedidoRepository.buscarPorId(PedidoId.de(pedidoId))
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));

        pedido.confirmarSincronizacao();
        Pedido pedidoAtualizado = pedidoRepository.atualizar(pedido);

        return converterParaResponse(pedidoAtualizado);
    }

    /**
     * Caso de uso: Registrar erro na sincronização
     */
    @Transactional
    public PedidoResponseDTO registrarErroSincronizacao(String pedidoId, String mensagemErro) {
        Pedido pedido = pedidoRepository.buscarPorId(PedidoId.de(pedidoId))
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));

        pedido.registrarErro(mensagemErro);
        Pedido pedidoAtualizado = pedidoRepository.atualizar(pedido);

        return converterParaResponse(pedidoAtualizado);
    }
    
    /**
     * Caso de uso: Sincronizar pedido com SAP via RFC (síncrono)
     * RFC garante consistência imediata
     */
    @Transactional
    public PedidoResponseDTO sincronizarComSapRfc(String pedidoId) {
        logger.info("Iniciando sincronização RFC com SAP para pedido: {}", pedidoId);
        
        Pedido pedido = pedidoRepository.buscarPorId(PedidoId.de(pedidoId))
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));
        
        try {
            // Chamada síncrona ao SAP via RFC
            String transacaoId = sapSyncPort.sincronizarPedidoRfc(pedido);
            
            // Transição de estado: VALIDADO → SINCRONIZANDO → SINCRONIZADO
            pedido.iniciarSincronizacao();
            pedido.confirmarSincronizacao();
            
            Pedido pedidoSincronizado = pedidoRepository.atualizar(pedido);
            
            logger.info("Pedido sincronizado com RFC. Transação SAP: {}", transacaoId);
            return converterParaResponse(pedidoSincronizado);
            
        } catch (SapSyncPort.SapSyncException e) {
            logger.error("Falha na sincronização RFC: {}", e.getMessage());
            pedido.iniciarSincronizacao();
            pedido.registrarErro("Erro RFC SAP: " + e.getMessage());
            pedidoRepository.atualizar(pedido);
            
            throw new SyncComSapException("Falha ao sincronizar com SAP: " + e.getMessage(), e);
        }
    }
    
    /**
     * Caso de uso: Publicar pedido em iDoc para SAP (assíncrono)
     * iDoc permite integração desacoplada e tolerante a falhas
     */
    @Transactional
    public PedidoResponseDTO publicarPedidoIdoc(String pedidoId) {
        logger.info("Publicando iDoc para pedido: {}", pedidoId);
        
        Pedido pedido = pedidoRepository.buscarPorId(PedidoId.de(pedidoId))
                .orElseThrow(() -> new PedidoNaoEncontradoException(pedidoId));
        
        try {
            // Publicação assíncrona via iDoc
            String idocId = sapSyncPort.publicarPedidoIdoc(pedido);
            
            // Marca pedido como em sincronização (processamento assíncrono)
            pedido.iniciarSincronizacao();
            Pedido pedidoPublicado = pedidoRepository.atualizar(pedido);
            
            logger.info("Pedido publicado como iDoc. ID: {}", idocId);
            return converterParaResponse(pedidoPublicado);
            
        } catch (SapSyncPort.SapSyncException e) {
            logger.error("Falha na publicação iDoc: {}", e.getMessage());
            pedido.registrarErro("Erro ao publicar iDoc: " + e.getMessage());
            pedidoRepository.atualizar(pedido);
            
            throw new SyncComSapException("Falha ao publicar iDoc: " + e.getMessage(), e);
        }
    }

    // Métodos auxiliares

    private PedidoResponseDTO converterParaResponse(Pedido pedido) {
        List<ItemPedidoDTO> itensDto = pedido.getItens().stream()
                .map(item -> new ItemPedidoDTO(
                        item.getCodigoProduto(),
                        item.getDescricao(),
                        item.getQuantidade(),
                        item.getPrecoUnitario()))
                .toList();

        return new PedidoResponseDTO(
                pedido.getPedidoId(),
                pedido.getCodigoCliente(),
                pedido.getStatus().toString(),
                itensDto,
                pedido.getValorTotal(),
                pedido.getCriadoEm());
    }

    /**
     * Exceção de domínio: Pedido não encontrado
     */
    public static class PedidoNaoEncontradoException extends RuntimeException {
        public PedidoNaoEncontradoException(String pedidoId) {
            super("Pedido com ID " + pedidoId + " não encontrado");
        }
    }
    
    /**
     * Exceção de aplicação: Erro na sincronização com SAP
     */
    public static class SyncComSapException extends RuntimeException {
        public SyncComSapException(String message) {
            super(message);
        }
        
        public SyncComSapException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
