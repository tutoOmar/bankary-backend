package com.bankary.cuenta.domain.model.strategy;

import com.bankary.cuenta.domain.model.TipoCuenta;

public class CuentaStrategyFactory {
    public static CuentaStrategy getStrategy(TipoCuenta tipoCuenta) {
        return switch (tipoCuenta) {
            case AHORRO -> new AhorroStrategy();
            case CORRIENTE -> new CorrienteStrategy();
        };
    }
}
