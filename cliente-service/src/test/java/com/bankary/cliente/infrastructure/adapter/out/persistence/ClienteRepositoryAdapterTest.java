package com.bankary.cliente.infrastructure.adapter.out.persistence;

import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.model.TipoDocumento;
import com.bankary.cliente.infrastructure.adapter.out.persistence.entity.ClienteEntity;
import com.bankary.cliente.infrastructure.adapter.out.persistence.repository.JpaClienteRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteRepositoryAdapter Unit Tests")
class ClienteRepositoryAdapterTest {

    @Mock
    private JpaClienteRepository jpaRepository;

    @InjectMocks
    private ClienteRepositoryAdapter adapter;

    @Test
    @DisplayName("findById - Success")
    void findById_Exitoso() {
        UUID id = UUID.randomUUID();
        ClienteEntity entity = ClienteEntity.builder().clienteId(id).nombre("Test").build();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Cliente> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getNombre());
    }

    @Test
    @DisplayName("findByDocumento - Success")
    void findByDocumento_Exitoso() {
        ClienteEntity entity = ClienteEntity.builder().tipoDocumento(TipoDocumento.CC).numeroDocumento("123").build();
        when(jpaRepository.findByTipoDocumentoAndNumeroDocumento(TipoDocumento.CC, "123")).thenReturn(Optional.of(entity));

        Optional<Cliente> result = adapter.findByDocumento(TipoDocumento.CC, "123");

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("findAllActivos - Success")
    void findAllActivos_Exitoso() {
        when(jpaRepository.findByEstadoTrue()).thenReturn(List.of(new ClienteEntity()));

        List<Cliente> result = adapter.findAllActivos();

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("save - Success")
    void save_Exitoso() {
        Cliente domain = Cliente.builder().nombre("Save").build();
        ClienteEntity entity = ClienteEntity.builder().nombre("Save").build();
        when(jpaRepository.save(any())).thenReturn(entity);

        Cliente result = adapter.save(domain);

        assertEquals("Save", result.getNombre());
        verify(jpaRepository).save(any());
    }

    @Test
    @DisplayName("deleteById - Success")
    void deleteById_Exitoso() {
        UUID id = UUID.randomUUID();
        doNothing().when(jpaRepository).deleteById(id);

        adapter.deleteById(id);

        verify(jpaRepository).deleteById(id);
    }
}
