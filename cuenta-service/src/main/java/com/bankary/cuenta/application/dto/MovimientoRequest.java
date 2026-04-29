package com.bankary.cuenta.application.dto;

import com.bankary.cuenta.domain.model.Movimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MovimientoRequest {
    @NotBlank
    private String numeroCuenta;
    
    @NotNull
    private Movimiento.TipoMovimiento tipoMovimiento;
    
    @NotNull
    @Positive
    private BigDecimal valor;
}
