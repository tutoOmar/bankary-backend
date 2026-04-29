package com.bankary.cuenta.domain;

import com.bankary.cuenta.domain.exception.CuentaDuplicadaException;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.TipoCuenta;

import java.util.List;

public class CuentaLimiteValidator {

    public static void validarLimitePorTipo(TipoCuenta tipoCuenta, List<Cuenta> cuentasActivas) {
        long count = cuentasActivas.stream()
                .filter(c -> c.getTipoCuenta() == tipoCuenta)
                .count();

        if (count >= 1) {
            throw new CuentaDuplicadaException("El cliente ya tiene una cuenta de tipo " + tipoCuenta + " activa");
        }
    }
}
