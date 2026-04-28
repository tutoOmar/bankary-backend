package com.tupackage.clienteservice.domain.port.in;

import com.tupackage.clienteservice.domain.model.Cliente;

import java.util.List;
import java.util.UUID;

public interface ClienteUseCase {
    Cliente create(Cliente cliente);
    Cliente update(UUID id, Cliente cliente);
    Cliente findById(UUID id);
    List<Cliente> findAllActive();
    Cliente deleteLogical(UUID id);
}
