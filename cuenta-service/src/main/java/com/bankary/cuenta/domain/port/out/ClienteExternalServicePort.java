package com.bankary.cuenta.domain.port.out;

import com.bankary.cuenta.domain.model.ClienteSnapshot;
import java.util.Optional;
import java.util.UUID;

public interface ClienteExternalServicePort {
    Optional<ClienteSnapshot> findClienteById(UUID clienteId);
}
