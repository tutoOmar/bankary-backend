package com.bankary.cliente.domain.port.out;

import com.bankary.cliente.domain.model.Cliente;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {
    Optional<Cliente> findById(UUID id);
    Optional<Cliente> findByIdentificacion(String identificacion);
    List<Cliente> findAllActivos();
    Cliente save(Cliente cliente);
    void deleteById(UUID id);
}
