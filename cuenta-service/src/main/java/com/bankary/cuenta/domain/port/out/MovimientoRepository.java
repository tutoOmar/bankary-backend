package com.bankary.cuenta.domain.port.out;

import com.bankary.cuenta.domain.model.Movimiento;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MovimientoRepository {
    Movimiento save(Movimiento movimiento);
    List<Movimiento> findAll();
    Optional<Movimiento> findById(UUID id);
    List<Movimiento> findByCuentaIdAndFechaBetween(UUID cuentaId, Instant start, Instant end);
}
