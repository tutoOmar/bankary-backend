package com.tupackage.clienteservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:cliente.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue:cliente.event.queue}")
    private String queueName;

    @Bean
    public TopicExchange clienteExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue clienteQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding binding(Queue clienteQueue, TopicExchange clienteExchange) {
        return BindingBuilder.bind(clienteQueue).to(clienteExchange).with("cliente.#");
    }
}
