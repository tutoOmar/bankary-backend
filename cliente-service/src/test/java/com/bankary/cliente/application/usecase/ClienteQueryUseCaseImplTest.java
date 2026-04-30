package com.bankary.cliente.application.usecase;

import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.exception.ResourceNotFoundException;
import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.port.out.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteQueryUseCaseImpl Unit Tests")
class ClienteQueryUseCaseImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteQueryUseCaseImpl useCase;

    private Cliente cliente;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cliente = Cliente.builder()
                .clienteId(clienteId)
                .nombre("Juan Perez")
                .estado(true)
                .build();
    }

    @Test
    @DisplayName("getById: Happy Path")
    void getById_Exitoso() {
        // Arrange
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        ClienteResponse result = useCase.getById(clienteId);

        // Assert
        assertNotNull(result);
        assertEquals(clienteId, result.getClienteId());
        assertEquals("Juan Perez", result.getNombre());
    }

    @Test
    @DisplayName("getById: Inactive client throws ResourceNotFoundException")
    void getById_Inactivo_LanzaResourceNotFoundException() {
        // Arrange
        cliente.setEstado(false);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.getById(clienteId));
    }

    @Test
    @DisplayName("getById: Not found throws ResourceNotFoundException")
    void getById_NoEncontrado_LanzaResourceNotFoundException() {
        // Arrange
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.getById(clienteId));
    }

    @Test
    @DisplayName("list: Happy Path")
    void list_Exitoso() {
        // Arrange
        when(clienteRepository.findAllActivos()).thenReturn(List.of(cliente));

        // Act
        List<ClienteResponse> result = useCase.list();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(clienteId, result.get(0).getClienteId());
    }
}
