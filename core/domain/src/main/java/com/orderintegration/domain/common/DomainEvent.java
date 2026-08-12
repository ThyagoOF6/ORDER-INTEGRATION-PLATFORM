package com.orderintegration.domain.common;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class para eventos de domínio.
 * 
 * Padrão: Domain Event
 * Permite que agregados comuniquem mudanças importantes através de eventos.
 */
public abstract class DomainEvent {

    private final String id;
    private final Instant ocorridoEm;
    private final String nomeEvento;

    protected DomainEvent(String nomeEvento) {
        this.id = java.util.UUID.randomUUID().toString();
        this.ocorridoEm = Instant.now();
        this.nomeEvento = Objects.requireNonNull(nomeEvento, "Nome do evento é obrigatório");
    }

    public String id() {
        return id;
    }

    public Instant ocorridoEm() {
        return ocorridoEm;
    }

    public String nomeEvento() {
        return nomeEvento;
    }

    @Override
    public String toString() {
        return "DomainEvent{" +
                "nomeEvento='" + nomeEvento + '\'' +
                ", ocorridoEm=" + ocorridoEm +
                '}';
    }
}
