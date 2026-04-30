package com.bankary.cuenta.domain.model.strategy;

import java.math.BigDecimal;

public interface CuentaStrategy {
    void validarRetiro(BigDecimal saldoDisponible, BigDecimal valorRetiro);
}
