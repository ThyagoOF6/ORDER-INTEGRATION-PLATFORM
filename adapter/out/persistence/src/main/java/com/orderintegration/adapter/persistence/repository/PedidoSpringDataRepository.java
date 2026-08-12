package com.orderintegration.adapter.persistence.repository;

import com.orderintegration.adapter.persistence.entity.PedidoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository para Pedido
 */
@Repository
public interface PedidoSpringDataRepository extends JpaRepository<PedidoJpaEntity, String> {
}
