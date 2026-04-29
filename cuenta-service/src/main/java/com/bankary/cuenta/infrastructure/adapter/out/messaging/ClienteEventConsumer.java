package com.bankary.cuenta.infrastructure.adapter.out.messaging;

import com.bankary.cuenta.domain.model.ClienteSnapshot;
import com.bankary.cuenta.domain.port.out.ClienteSnapshotRepository;
import com.bankary.cuenta.infrastructure.config.RabbitMQConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventConsumer {

    private final ClienteSnapshotRepository repository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleClienteEvent(ClienteEvent event) {
        log.info("Received event: {}", event);
        ClienteSnapshot snapshot = ClienteSnapshot.builder()
                .clienteId(event.getClienteId())
                .nombre(event.getNombre())
                .lastUpdated(event.getTimestamp())
                .build();
        repository.save(snapshot);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClienteEvent {
        private UUID clienteId;
        private String nombre;
        private Instant timestamp;
        private String eventType;
    }
}
