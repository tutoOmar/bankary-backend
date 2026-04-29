package com.bankary.cuenta.domain.port.in;

import com.bankary.cuenta.domain.model.Movimiento;
import java.util.List;
import java.util.UUID;

public interface MovimientoUseCase {
    Movimiento registrarMovimiento(String numeroCuenta, Movimiento movimiento);
    List<Movimiento> list();
    Movimiento getById(UUID id);
}
