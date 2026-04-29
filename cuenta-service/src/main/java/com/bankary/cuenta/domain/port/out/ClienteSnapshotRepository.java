package com.bankary.cuenta.domain.port.out;

import com.bankary.cuenta.domain.model.ClienteSnapshot;
import java.util.Optional;
import java.util.UUID;

public interface ClienteSnapshotRepository {
    ClienteSnapshot save(ClienteSnapshot snapshot);
    Optional<ClienteSnapshot> findById(UUID clienteId);
}
