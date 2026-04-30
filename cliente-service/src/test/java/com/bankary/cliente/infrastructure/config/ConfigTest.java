package com.bankary.cliente.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@DisplayName("Infrastructure Config Tests")
class ConfigTest {

    private final RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();

    @Test
    @DisplayName("RabbitMQConfig beans initialization")
    void rabbitMQConfig_Beans() {
        assertNotNull(rabbitMQConfig.clienteExchange());
        assertNotNull(rabbitMQConfig.jsonMessageConverter());
        assertNotNull(rabbitMQConfig.rabbitTemplate(mock(ConnectionFactory.class)));
    }
}
