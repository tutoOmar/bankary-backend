package com.bankary.cliente.domain.port.out;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface ClienteEventPublisher {
    
    void publishClienteCreado(ClienteEvent event);
    void publishClienteActualizado(ClienteEvent event);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class ClienteEvent {
        private UUID clienteId;
        private String nombre;
        private Instant timestamp;
        private String eventType;
    }
}
