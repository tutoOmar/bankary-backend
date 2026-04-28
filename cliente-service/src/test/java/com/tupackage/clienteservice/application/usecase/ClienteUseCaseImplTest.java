package com.tupackage.clienteservice.application.usecase;

import com.tupackage.clienteservice.domain.model.Cliente;
import com.tupackage.clienteservice.domain.port.out.ClienteEventPublisher;
import com.tupackage.clienteservice.domain.port.out.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClienteUseCaseImplTest {

    @Mock
    ClienteRepository repository;

    @Mock
    ClienteEventPublisher publisher;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    ClienteUseCaseImpl useCase;

    @Test
    void create_calls_repository_and_publishes_event() {
        Cliente c = new Cliente();
        c.setNombre("Juan");
        c.setEdad(30);
        c.setIdentificacion("ID-123");
        c.setPassword("secret");

        when(repository.findByIdentificacion("ID-123")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("HASH");
        when(repository.save(any())).thenAnswer(invocation -> {
            Cliente arg = invocation.getArgument(0);
            arg.setId(UUID.randomUUID());
            return arg;
        });

        Cliente saved = useCase.create(c);

        verify(repository, times(1)).save(any());
        verify(publisher, times(1)).publish(any());
        assertNotNull(saved.getId());
    }
}
