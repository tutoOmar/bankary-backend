package com.bankary.cliente.infrastructure.adapter.out.messaging;

import com.bankary.cliente.domain.port.out.ClienteEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RabbitMQEventPublisherAdapter Unit Tests")
class RabbitMQEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQEventPublisherAdapter adapter;

    @Test
    @DisplayName("publishClienteCreado - Success")
    void publishClienteCreado_Exitoso() {
        ClienteEventPublisher.ClienteEvent event = new ClienteEventPublisher.ClienteEvent(
                UUID.randomUUID(), "Test", Instant.now(), "created");

        adapter.publishClienteCreado(event);

        verify(rabbitTemplate).convertAndSend(anyString(), eq("cliente.created"), eq(event));
    }

    @Test
    @DisplayName("publishClienteActualizado - Success")
    void publishClienteActualizado_Exitoso() {
        ClienteEventPublisher.ClienteEvent event = new ClienteEventPublisher.ClienteEvent(
                UUID.randomUUID(), "Test", Instant.now(), "updated");

        adapter.publishClienteActualizado(event);

        verify(rabbitTemplate).convertAndSend(anyString(), eq("cliente.updated"), eq(event));
    }
}
