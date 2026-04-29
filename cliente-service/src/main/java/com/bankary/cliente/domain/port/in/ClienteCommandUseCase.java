package com.bankary.cliente.domain.port.in;

import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.dto.CreateClienteCommand;
import com.bankary.cliente.application.dto.UpdateClienteCommand;

import java.util.UUID;

public interface ClienteCommandUseCase {
    ClienteResponse create(CreateClienteCommand command);
    ClienteResponse update(UUID clienteId, UpdateClienteCommand command);
    void delete(UUID clienteId);
}
