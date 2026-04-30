package com.bankary.cuenta.domain.port.out;

import com.bankary.cuenta.domain.model.Cuenta;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CuentaRepository {
    Cuenta save(Cuenta cuenta);
    Optional<Cuenta> findById(UUID id);
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
    List<Cuenta> findByClienteIdAndEstadoTrue(UUID clienteId);
    List<Cuenta> findAll();
    void deleteByNumeroCuenta(String numeroCuenta);
}
