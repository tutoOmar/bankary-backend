package com.bankary.cliente.application.usecase;

import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.dto.CreateClienteCommand;
import com.bankary.cliente.application.dto.UpdateClienteCommand;
import com.bankary.cliente.application.exception.ConflictException;
import com.bankary.cliente.application.exception.ResourceNotFoundException;
import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.port.out.ClienteEventPublisher;
import com.bankary.cliente.domain.port.out.ClienteRepository;
import com.bankary.cliente.domain.port.out.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cliente Command Use Case Tests")
class ClienteCommandUseCaseImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ClienteCommandUseCaseImpl clienteCommandUseCase;

    @BeforeEach
    void setUp() {
        clienteCommandUseCase = new ClienteCommandUseCaseImpl(clienteRepository, eventPublisher, passwordEncoder);
    }

    @Nested
    @DisplayName("Create Cliente Scenarios")
    class CreateScenarios {
        @Test
        @DisplayName("Success: Should create client and publish event")
        void create_success() {
            CreateClienteCommand command = CreateClienteCommand.builder()
                    .nombre("Juan Perez")
                    .identificacion("123456789")
                    .contrasena("pass123")
                    .build();

            when(clienteRepository.findByIdentificacion("123456789")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("pass123")).thenReturn("hashedPass");
            
            Cliente savedCliente = Cliente.builder()
                    .clienteId(UUID.randomUUID())
                    .nombre("Juan Perez")
                    .identificacion("123456789")
                    .estado(true)
                    .build();
            when(clienteRepository.save(any(Cliente.class))).thenReturn(savedCliente);

            ClienteResponse response = clienteCommandUseCase.create(command);

            assertNotNull(response);
            assertEquals("Juan Perez", response.getNombre());
            verify(eventPublisher).publishClienteCreado(any());
        }

        @Test
        @DisplayName("Resilience: Should not fail if Event Publisher throws Exception")
        void create_eventPublisherFailure_shouldStillSucceed() {
            CreateClienteCommand command = CreateClienteCommand.builder()
                    .identificacion("123456789")
                    .contrasena("pass123")
                    .build();

            when(clienteRepository.findByIdentificacion(any())).thenReturn(Optional.empty());
            when(clienteRepository.save(any())).thenReturn(new Cliente());
            doThrow(new RuntimeException("Kafka down")).when(eventPublisher).publishClienteCreado(any());

            assertDoesNotThrow(() -> clienteCommandUseCase.create(command));
            verify(clienteRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Update Cliente Scenarios")
    class UpdateScenarios {
        @Test
        @DisplayName("Conflict: Should throw exception if new identification belongs to another client")
        void update_conflictIdentification() {
            UUID clientId = UUID.randomUUID();
            UpdateClienteCommand command = UpdateClienteCommand.builder()
                    .identificacion("999999") // New ID
                    .build();

            Cliente existingInDb = Cliente.builder()
                    .clienteId(clientId)
                    .identificacion("111111") // Current ID
                    .build();

            Cliente anotherClient = Cliente.builder()
                    .clienteId(UUID.randomUUID()) // Different ID
                    .identificacion("999999")
                    .build();

            when(clienteRepository.findById(clientId)).thenReturn(Optional.of(existingInDb));
            when(clienteRepository.findByIdentificacion("999999")).thenReturn(Optional.of(anotherClient));

            assertThrows(ConflictException.class, () -> clienteCommandUseCase.update(clientId, command));
        }

        @Test
        @DisplayName("Success: Should NOT update password if field is blank")
        void update_blankPassword_shouldNotEncode() {
            UUID clientId = UUID.randomUUID();
            UpdateClienteCommand command = UpdateClienteCommand.builder()
                    .nombre("New Name")
                    .identificacion("111111")
                    .contrasena("") // Blank password
                    .build();

            Cliente existing = Cliente.builder()
                    .clienteId(clientId)
                    .identificacion("111111")
                    .contrasena("oldHashedPassword")
                    .build();

            when(clienteRepository.findById(clientId)).thenReturn(Optional.of(existing));
            when(clienteRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            ClienteResponse response = clienteCommandUseCase.update(clientId, command);

            verify(passwordEncoder, never()).encode(anyString());
            // In the real impl, it should keep the old one. We check repo save call.
            verify(clienteRepository).save(argThat(c -> c.getContrasena().equals("oldHashedPassword")));
        }
    }
}
