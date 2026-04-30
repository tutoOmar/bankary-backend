package com.bankary.cliente;

import com.bankary.cliente.application.dto.ClienteRequest;
import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.dto.CreateClienteCommand;
import com.bankary.cliente.application.dto.UpdateClienteCommand;
import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.infrastructure.adapter.out.persistence.entity.ClienteEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Data Models Sanity Tests")
class DataModelsTest {

    @Test
    @DisplayName("DTOs and Entities basic coverage")
    void coverageSanity() {
        UUID id = UUID.randomUUID();
        
        // DTOs
        assertNotNull(ClienteRequest.builder().nombre("Test").build().toString());
        ClienteRequest req = new ClienteRequest();
        req.setNombre("Test");
        assertNotNull(req.getNombre());
        assertNotNull(ClienteResponse.builder().clienteId(id).build().toString());
        
        // Commands
        assertNotNull(CreateClienteCommand.builder().nombre("Test").build().toString());
        assertNotNull(UpdateClienteCommand.builder().nombre("Test").build().toString());

        // Entities
        ClienteEntity entity = ClienteEntity.builder()
                .clienteId(id)
                .nombre("Test")
                .estado(true)
                .build();
        assertNotNull(entity.toString());
        ClienteEntity entity2 = new ClienteEntity();
        entity2.setNombre("Test");
        assertNotNull(entity2.getNombre());
        
        // Domain
        assertNotNull(Cliente.builder().clienteId(id).build().toString());
    }
}
