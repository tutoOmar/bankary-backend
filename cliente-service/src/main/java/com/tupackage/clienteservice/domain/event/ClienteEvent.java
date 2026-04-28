package com.tupackage.clienteservice.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio para cambios en la entidad Cliente.
 * Vive en la capa de dominio — sin dependencias de infraestructura.
 */
public class ClienteEvent {

    public enum Type {
        CLIENTE_CREATED, CLIENTE_UPDATED, CLIENTE_DELETED
    }

    private final UUID clienteId;
    private final String nombre;
    private final Instant occurredOn;
    private final Type type;

    public ClienteEvent(UUID clienteId, String nombre, Instant occurredOn, Type type) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.occurredOn = occurredOn;
        this.type = type;
    }

    public UUID getClienteId() { return clienteId; }
    public String getNombre()  { return nombre; }
    public Instant getOccurredOn() { return occurredOn; }
    public Type getType()      { return type; }
}
