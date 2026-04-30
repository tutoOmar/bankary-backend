package com.bankary.cliente.infrastructure.adapter.out.messaging;

import com.bankary.cliente.domain.port.out.ClienteEventPublisher;
import com.bankary.cliente.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisherAdapter implements ClienteEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishClienteCreado(ClienteEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "cliente.created", event);
    }

    @Override
    public void publishClienteActualizado(ClienteEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "cliente.updated", event);
    }
}
