package com.bankary.cuenta.domain;

import com.bankary.cuenta.domain.exception.CuentaDuplicadaException;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.TipoCuenta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CuentaLimiteValidatorTest {

    @Test
    @DisplayName("Permitir crear cuenta si no hay ninguna activa")
    void sinCuentas() {
        assertDoesNotThrow(() -> 
            CuentaLimiteValidator.validarLimitePorTipo(TipoCuenta.AHORRO, Collections.emptyList()));
    }

    @Test
    @DisplayName("Permitir crear cuenta CORRIENTE si ya tiene una de AHORRO")
    void ahorroExistentePermiteCorriente() {
        List<Cuenta> activas = List.of(
            Cuenta.builder().tipoCuenta(TipoCuenta.AHORRO).estado(true).build()
        );
        assertDoesNotThrow(() -> 
            CuentaLimiteValidator.validarLimitePorTipo(TipoCuenta.CORRIENTE, activas));
    }

    @Test
    @DisplayName("Error al intentar crear segunda cuenta de AHORRO")
    void ahorroExistenteBloqueaAhorro() {
        List<Cuenta> activas = List.of(
            Cuenta.builder().tipoCuenta(TipoCuenta.AHORRO).estado(true).build()
        );
        
        CuentaDuplicadaException ex = assertThrows(CuentaDuplicadaException.class, 
            () -> CuentaLimiteValidator.validarLimitePorTipo(TipoCuenta.AHORRO, activas));
        
        assertEquals("El cliente ya tiene una cuenta de tipo AHORRO activa", ex.getMessage());
    }

    @Test
    @DisplayName("Error al intentar crear segunda cuenta CORRIENTE")
    void corrienteExistenteBloqueaCorriente() {
        List<Cuenta> activas = List.of(
            Cuenta.builder().tipoCuenta(TipoCuenta.CORRIENTE).estado(true).build()
        );
        
        CuentaDuplicadaException ex = assertThrows(CuentaDuplicadaException.class, 
            () -> CuentaLimiteValidator.validarLimitePorTipo(TipoCuenta.CORRIENTE, activas));
        
        assertEquals("El cliente ya tiene una cuenta de tipo CORRIENTE activa", ex.getMessage());
    }
}
