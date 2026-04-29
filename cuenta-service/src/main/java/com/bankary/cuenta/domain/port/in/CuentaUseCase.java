package com.bankary.cuenta.domain.port.in;

import com.bankary.cuenta.domain.model.Cuenta;
import java.util.List;
import java.util.UUID;

public interface CuentaUseCase {
    Cuenta create(Cuenta cuenta);
    Cuenta update(String numeroCuenta, Cuenta cuenta);
    void delete(String numeroCuenta);
    Cuenta getByNumeroCuenta(String numeroCuenta);
    List<Cuenta> list();
}
