package com.bankary.cliente.domain.port.out;

import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.model.TipoDocumento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteReader {
    Optional<Cliente> findById(UUID id);
    Optional<Cliente> findByDocumento(TipoDocumento tipoDocumento, String numeroDocumento);
    List<Cliente> findAllActivos();
}
