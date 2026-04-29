package com.bankary.cliente.domain.port.in;

import com.bankary.cliente.application.dto.ClienteResponse;

import java.util.List;
import java.util.UUID;

public interface ClienteQueryUseCase {
    ClienteResponse getById(UUID clienteId);
    List<ClienteResponse> list();
}
