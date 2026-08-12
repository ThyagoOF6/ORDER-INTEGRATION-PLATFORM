package com.orderintegration.adapter.persistence.repository;

import com.orderintegration.adapter.persistence.entity.PedidoJpaEntity;
import com.orderintegration.application.port.PedidoRepositoryPort;
import com.orderintegration.core.domain.order.Pedido;
import com.orderintegration.core.domain.order.PedidoId;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador que implementa PedidoRepositoryPort usando Spring Data JPA
 * Hexagonal Architecture: Adapter (out/persistence)
 */
@Component
public class PedidoJpaRepositoryAdapter implements PedidoRepositoryPort {

    private final PedidoSpringDataRepository springDataRepository;

    public PedidoJpaRepositoryAdapter(PedidoSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Pedido salvar(Pedido pedido) {
        PedidoJpaEntity entity = new PedidoJpaEntity(pedido);
        PedidoJpaEntity entidadeSalva = springDataRepository.save(entity);
        return entidadeSalva.toDomain();
    }

    @Override
    public Optional<Pedido> buscarPorId(PedidoId pedidoId) {
        return springDataRepository.findById(pedidoId.getValor())
                .map(PedidoJpaEntity::toDomain);
    }

    @Override
    public Pedido atualizar(Pedido pedido) {
        PedidoJpaEntity entity = new PedidoJpaEntity(pedido);
        PedidoJpaEntity entidadeAtualizada = springDataRepository.save(entity);
        return entidadeAtualizada.toDomain();
    }

    @Override
    public boolean existe(PedidoId pedidoId) {
        return springDataRepository.existsById(pedidoId.getValor());
    }
}
