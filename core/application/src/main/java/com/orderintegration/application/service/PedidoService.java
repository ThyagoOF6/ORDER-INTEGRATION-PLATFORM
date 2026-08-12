package com.orderintegration.application.service;

import com.orderintegration.application.dto.ItemPedidoDTO;
import com.orderintegration.application.dto.PedidoRequestDTO;
import com.orderintegration.application.dto.PedidoResponseDTO;
import com.orderintegration.application.port.PedidoRepositoryPort;
import com.orderintegration.core.domain.order.ItemPedido;
import com.orderintegration.core.domain.order.Pedido;
import com.orderintegration.core.domain.order.PedidoId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de aplicação para casos de uso do Pedido
 * Orquestra entre Application layer e Domain layer
 */
@Service
public class PedidoService {

    private final PedidoRepositoryPort pedidoRepository;

    public PedidoService(PedidoRepositoryPort pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
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
}
