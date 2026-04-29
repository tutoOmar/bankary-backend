package com.bankary.cuenta.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "cliente.exchange";
    public static final String QUEUE_NAME = "cuenta.cliente.queue";

    @Bean
    public TopicExchange clienteExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue cuentaClienteQueue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public Binding bindingCreated(Queue cuentaClienteQueue, TopicExchange clienteExchange) {
        return BindingBuilder.bind(cuentaClienteQueue).to(clienteExchange).with("cliente.created");
    }

    @Bean
    public Binding bindingUpdated(Queue cuentaClienteQueue, TopicExchange clienteExchange) {
        return BindingBuilder.bind(cuentaClienteQueue).to(clienteExchange).with("cliente.updated");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
