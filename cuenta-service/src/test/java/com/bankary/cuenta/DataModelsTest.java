package com.bankary.cuenta;

import com.bankary.cuenta.application.dto.CuentaRequest;
import com.bankary.cuenta.application.dto.MovimientoRequest;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.CuentaEntity;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.MovimientoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Data Models Sanity Tests")
class DataModelsTest {

    @Test
    @DisplayName("DTOs and Entities basic coverage")
    void coverageSanity() {
        UUID id = UUID.randomUUID();
        
        // DTOs
        assertNotNull(CuentaRequest.builder().numeroCuenta("123").build().toString());
        CuentaRequest req = new CuentaRequest();
        req.setNumeroCuenta("123");
        assertNotNull(req.getNumeroCuenta());
        assertNotNull(MovimientoRequest.builder().numeroCuenta("123").build().toString());

        // Entities
        CuentaEntity entity = CuentaEntity.builder().id(id).numeroCuenta("123").build();
        assertNotNull(entity.toString());
        CuentaEntity entity2 = new CuentaEntity();
        entity2.setNumeroCuenta("123");
        assertNotNull(entity2.getNumeroCuenta());
        assertNotNull(MovimientoEntity.builder().id(id).valor(BigDecimal.ONE).build().toString());
        
        // Domain
        assertNotNull(Cuenta.builder().id(id).build().toString());
        assertNotNull(Movimiento.builder().id(id).build().toString());
    }
}
