package com.bankary.cuenta.domain.model.strategy;

import com.bankary.cuenta.application.exception.SaldoInsuficienteException;
import com.bankary.cuenta.domain.model.TipoCuenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StrategyPatternTest {

    @Test
    void ahorroStrategy_WhenSaldoIsEnough_DoesNotThrow() {
        CuentaStrategy strategy = new AhorroStrategy();
        assertDoesNotThrow(() -> strategy.validarRetiro(new BigDecimal("100"), new BigDecimal("50")));
    }

    @Test
    void ahorroStrategy_WhenSaldoIsExact_DoesNotThrow() {
        CuentaStrategy strategy = new AhorroStrategy();
        assertDoesNotThrow(() -> strategy.validarRetiro(new BigDecimal("100"), new BigDecimal("100")));
    }

    @Test
    void ahorroStrategy_WhenSaldoIsInsufficient_ThrowsException() {
        CuentaStrategy strategy = new AhorroStrategy();
        assertThrows(SaldoInsuficienteException.class, () -> strategy.validarRetiro(new BigDecimal("100"), new BigDecimal("100.01")));
    }

    @Test
    void corrienteStrategy_WhenSaldoIsEnough_DoesNotThrow() {
        CuentaStrategy strategy = new CorrienteStrategy();
        assertDoesNotThrow(() -> strategy.validarRetiro(new BigDecimal("100"), new BigDecimal("50")));
    }

    @Test
    void corrienteStrategy_WhenSaldoIsInsufficient_ThrowsException() {
        CuentaStrategy strategy = new CorrienteStrategy();
        assertThrows(SaldoInsuficienteException.class, () -> strategy.validarRetiro(new BigDecimal("100"), new BigDecimal("1000")));
    }

    @Test
    void factory_ReturnsCorrectStrategy() {
        // Just checking the factory logic
        assertNotNull(CuentaStrategyFactory.getStrategy(TipoCuenta.AHORRO));
        assertNotNull(CuentaStrategyFactory.getStrategy(TipoCuenta.CORRIENTE));
    }

    private void assertNotNull(Object obj) {
        if (obj == null) throw new AssertionError("Object is null");
    }
}
