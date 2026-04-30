package com.bankary.cuenta.domain.model.strategy;

import com.bankary.cuenta.application.exception.SaldoInsuficienteException;
import java.math.BigDecimal;

public class CorrienteStrategy implements CuentaStrategy {
    @Override
    public void validarRetiro(BigDecimal saldoDisponible, BigDecimal valorRetiro) {
        if (saldoDisponible.compareTo(valorRetiro) < 0) {
            throw new SaldoInsuficienteException("Saldo no disponible");
        }
    }
}
