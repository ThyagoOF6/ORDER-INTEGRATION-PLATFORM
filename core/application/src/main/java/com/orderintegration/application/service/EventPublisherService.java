package com.orderintegration.application.service;

import com.orderintegration.application.dto.DomainEventDto;
import com.orderintegration.application.port.DomainEventRepositoryPort;
import com.orderintegration.core.domain.common.DomainEvent;
import com.orderintegration.core.domain.order.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * EventPublisherService - Serviço de aplicação para persistir eventos de
 * domínio no Event Store
 *
 * Responsabilidades:
 * - Extrair eventos de domínio pendentes de um agregado (Pedido)
 * - Converter DomainEvent (domínio) em DomainEventDto (Event Store)
 * - Persistir eventos via DomainEventRepositoryPort (Event Store)
 * - Limpar eventos do agregado após persistência
 *
 * Fluxo (Event Sourcing):
 * 1. Pedido.criar()/validar()/confirmarSincronizacao()/registrarErro() ->
 * adiciona DomainEvent na lista interna do agregado
 * 2. Após persistir o Pedido, PedidoService/IdocResponseService chama
 * publicarEventos(pedido)
 * 3. Este serviço persiste cada evento no Event Store (tabela domain_events)
 * 4. Um processo assíncrono (scheduler) publica eventos não publicados para o
 * Kafka
 *
 * @author Thyago Oliveira Ferreira
 * @version 1.0 - Phase 3 (Event Sourcing)
 * @since 2026-08-12
 */
@Service
public class EventPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);

    private final DomainEventRepositoryPort domainEventRepository;

    public EventPublisherService(DomainEventRepositoryPort domainEventRepository) {
        this.domainEventRepository = domainEventRepository;
    }

    /**
     * Persiste todos os eventos pendentes do Pedido no Event Store e limpa a
     * lista de eventos do agregado.
     *
     * @param pedido Agregado com eventos pendentes
     */
    @Transactional
    public void publicarEventos(Pedido pedido) {
        List<DomainEvent> eventos = pedido.getEventos();

        if (eventos.isEmpty()) {
            return;
        }

        for (DomainEvent evento : eventos) {
            DomainEventDto dto = DomainEventDto.builder()
                    .eventId(evento.getId())
                    .eventType(evento.getClass().getName())
                    .aggregateId(evento.getAggregateId())
                    .aggregateType("Pedido")
                    .payload(evento.toPayload())
                    .createdAt(evento.getOcorridoEm())
                    .correlationId(UUID.randomUUID().toString())
                    .isPublished(false)
                    .version(1)
                    .build();

            domainEventRepository.persistEvent(dto);

            logger.debug("Evento persistido no Event Store: eventId={}, eventType={}, aggregateId={}",
                    dto.getEventId(), dto.getEventType(), dto.getAggregateId());
        }

        pedido.limparEventos();
    }
}
