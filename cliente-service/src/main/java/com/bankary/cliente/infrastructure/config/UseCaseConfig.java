package com.bankary.cliente.infrastructure.config;

import com.bankary.cliente.application.usecase.ClienteCommandUseCaseImpl;
import com.bankary.cliente.application.usecase.ClienteQueryUseCaseImpl;
import com.bankary.cliente.domain.port.in.ClienteCommandUseCase;
import com.bankary.cliente.domain.port.in.ClienteQueryUseCase;
import com.bankary.cliente.domain.port.out.ClienteEventPublisher;
import com.bankary.cliente.domain.port.out.ClienteRepository;
import com.bankary.cliente.domain.port.out.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ClienteCommandUseCase clienteCommandUseCase(
            ClienteRepository clienteRepository, 
            ClienteEventPublisher eventPublisher,
            PasswordEncoder passwordEncoder) {
        return new ClienteCommandUseCaseImpl(clienteRepository, eventPublisher, passwordEncoder);
    }

    @Bean
    public ClienteQueryUseCase clienteQueryUseCase(ClienteRepository clienteRepository) {
        return new ClienteQueryUseCaseImpl(clienteRepository);
    }
}
