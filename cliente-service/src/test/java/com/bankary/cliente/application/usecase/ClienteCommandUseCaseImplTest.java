package com.bankary.cliente.application.usecase;

import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.dto.CreateClienteCommand;
import com.bankary.cliente.application.dto.UpdateClienteCommand;
import com.bankary.cliente.application.exception.ConflictException;
import com.bankary.cliente.application.exception.ResourceNotFoundException;
import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.model.TipoDocumento;
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
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("1023456789")
                    .edad(30)
                    .contrasena("pass123")
                    .build();

            when(clienteRepository.findByDocumento(TipoDocumento.CC, "1023456789")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("pass123")).thenReturn("hashedPass");
            
            Cliente savedCliente = Cliente.builder()
                    .clienteId(UUID.randomUUID())
                    .nombre("Juan Perez")
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("1023456789")
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
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("1023456789")
                    .edad(30)
                    .contrasena("pass123")
                    .build();

            when(clienteRepository.findByDocumento(any(), any())).thenReturn(Optional.empty());
            when(clienteRepository.save(any())).thenReturn(new Cliente());
            doThrow(new RuntimeException("Kafka down")).when(eventPublisher).publishClienteCreado(any());

            assertDoesNotThrow(() -> clienteCommandUseCase.create(command));
            verify(clienteRepository).save(any());
        }
        @Test
        @DisplayName("Conflict: Should throw exception if client already exists")
        void create_duplicateClient_shouldThrowConflict() {
            CreateClienteCommand command = CreateClienteCommand.builder()
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("12345678")
                    .edad(25)
                    .build();

            when(clienteRepository.findByDocumento(any(), any())).thenReturn(Optional.of(new Cliente()));

            assertThrows(ConflictException.class, () -> clienteCommandUseCase.create(command));
        }
    }

    @Nested
    @DisplayName("Update Cliente Scenarios")
    class UpdateScenarios {
        @Test
        @DisplayName("Success: Should update client and publish event")
        void update_success() {
            UUID clientId = UUID.randomUUID();
            UpdateClienteCommand command = UpdateClienteCommand.builder()
                    .nombre("Updated Name")
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("11111111")
                    .edad(30)
                    .contrasena("newPass")
                    .build();

            Cliente existing = Cliente.builder()
                    .clienteId(clientId)
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("11111111")
                    .build();

            when(clienteRepository.findById(clientId)).thenReturn(Optional.of(existing));
            when(passwordEncoder.encode("newPass")).thenReturn("hashedNewPass");
            when(clienteRepository.save(any())).thenReturn(existing);

            clienteCommandUseCase.update(clientId, command);

            verify(clienteRepository).save(any());
            verify(eventPublisher).publishClienteActualizado(any());
        }

        @Test
        @DisplayName("Success: Should update client WITHOUT changing document")
        void update_noDocumentChange() {
            UUID clientId = UUID.randomUUID();
            UpdateClienteCommand command = UpdateClienteCommand.builder()
                    .nombre("Updated Name")
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("11111111")
                    .edad(30)
                    .build();

            Cliente existing = Cliente.builder()
                    .clienteId(clientId)
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("11111111")
                    .build();

            when(clienteRepository.findById(clientId)).thenReturn(Optional.of(existing));
            when(clienteRepository.save(any())).thenReturn(existing);

            clienteCommandUseCase.update(clientId, command);

            verify(clienteRepository, never()).findByDocumento(any(), any());
        }

        @Test
        @DisplayName("Resilience: Should not fail if Event Publisher throws Exception on update")
        void update_eventPublisherFailure_shouldStillSucceed() {
            UUID clientId = UUID.randomUUID();
            UpdateClienteCommand command = UpdateClienteCommand.builder()
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("11111111")
                    .edad(30)
                    .build();

            Cliente existing = Cliente.builder()
                    .clienteId(clientId)
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("11111111")
                    .build();

            when(clienteRepository.findById(clientId)).thenReturn(Optional.of(existing));
            when(clienteRepository.save(any())).thenReturn(existing);
            doThrow(new RuntimeException("Rabbit down")).when(eventPublisher).publishClienteActualizado(any());

            assertDoesNotThrow(() -> clienteCommandUseCase.update(clientId, command));
        }

        @Test
        @DisplayName("Not Found: Should throw exception if client doesn't exist")
        void update_notFound_shouldThrowException() {
            UUID clientId = UUID.randomUUID();
            when(clienteRepository.findById(clientId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clienteCommandUseCase.update(clientId, new UpdateClienteCommand()));
        }

        @Test
        @DisplayName("Conflict: Should throw exception if new identification belongs to another client")
        void update_conflictIdentification() {
            UUID clientId = UUID.randomUUID();
            UpdateClienteCommand command = UpdateClienteCommand.builder()
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("99999999") // New ID
                    .edad(30)
                    .build();

            Cliente existingInDb = Cliente.builder()
                    .clienteId(clientId)
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("11111111") // Current ID
                    .build();

            Cliente anotherClient = Cliente.builder()
                    .clienteId(UUID.randomUUID()) // Different ID
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("99999999")
                    .build();

            when(clienteRepository.findById(clientId)).thenReturn(Optional.of(existingInDb));
            when(clienteRepository.findByDocumento(TipoDocumento.CC, "99999999")).thenReturn(Optional.of(anotherClient));

            assertThrows(ConflictException.class, () -> clienteCommandUseCase.update(clientId, command));
        }

        @Test
        @DisplayName("Success: Should NOT update password if field is blank")
        void update_blankPassword_shouldNotEncode() {
            UUID clientId = UUID.randomUUID();
            UpdateClienteCommand command = UpdateClienteCommand.builder()
                    .nombre("New Name")
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("11111111")
                    .edad(30)
                    .contrasena("") // Blank password
                    .build();

            Cliente existing = Cliente.builder()
                    .clienteId(clientId)
                    .tipoDocumento(TipoDocumento.CC)
                    .numeroDocumento("11111111")
                    .contrasena("oldHashedPassword")
                    .build();

            when(clienteRepository.findById(clientId)).thenReturn(Optional.of(existing));
            when(clienteRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

            clienteCommandUseCase.update(clientId, command);

            verify(passwordEncoder, never()).encode(anyString());
            verify(clienteRepository).save(argThat(c -> c.getContrasena().equals("oldHashedPassword")));
        }
    }

    @Nested
    @DisplayName("Delete Cliente Scenarios")
    class DeleteScenarios {
        @Test
        @DisplayName("Success: Should perform soft delete")
        void delete_success() {
            UUID clientId = UUID.randomUUID();
            Cliente existing = Cliente.builder().clienteId(clientId).estado(true).build();

            when(clienteRepository.findById(clientId)).thenReturn(Optional.of(existing));

            clienteCommandUseCase.delete(clientId);

            assertFalse(existing.isEstado());
            verify(clienteRepository).save(existing);
        }

        @Test
        @DisplayName("Not Found: Should throw exception")
        void delete_notFound_shouldThrowException() {
            UUID clientId = UUID.randomUUID();
            when(clienteRepository.findById(clientId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clienteCommandUseCase.delete(clientId));
        }
    }
}
