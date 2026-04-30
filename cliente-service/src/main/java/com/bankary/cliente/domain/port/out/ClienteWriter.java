package com.bankary.cliente.domain.port.out;

import com.bankary.cliente.domain.model.Cliente;

import java.util.UUID;

public interface ClienteWriter {
    Cliente save(Cliente cliente);
    void deleteById(UUID id);
}
