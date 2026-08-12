package com.orderintegration.core.domain.common;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe abstrata base para todos os Eventos de Domínio
 * Implementa padrão de Domain Event do DDD
 */
public abstract class DomainEvent {

    private final String id;
    private final Instant ocorridoEm;
    private final String nomeEvento;

    protected DomainEvent() {
        this.id = UUID.randomUUID().toString();
        this.ocorridoEm = Instant.now();
        this.nomeEvento = this.getClass().getSimpleName();
    }

    public String getId() {
        return id;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }

    public String getNomeEvento() {
        return nomeEvento;
    }

    /**
     * ID do agregado que originou o evento (ex.: pedidoId)
     */
    public abstract String getAggregateId();

    /**
     * Representação do evento como payload serializável para o Event Store
     */
    public abstract Map<String, Object> toPayload();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DomainEvent that = (DomainEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s{id='%s', ocorridoEm=%s}", nomeEvento, id, ocorridoEm);
    }
}
