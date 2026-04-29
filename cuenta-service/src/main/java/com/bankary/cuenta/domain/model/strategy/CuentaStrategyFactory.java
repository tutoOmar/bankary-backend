package com.bankary.cuenta.domain.model.strategy;

import com.bankary.cuenta.domain.model.Cuenta;

public class CuentaStrategyFactory {
    public static CuentaStrategy getStrategy(Cuenta.TipoCuenta tipoCuenta) {
        return switch (tipoCuenta) {
            case AHORRO -> new AhorroStrategy();
            case CORRIENTE -> new CorrienteStrategy();
        };
    }
}
