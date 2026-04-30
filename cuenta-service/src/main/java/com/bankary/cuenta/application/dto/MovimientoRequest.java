package com.bankary.cuenta.application.dto;

import com.bankary.cuenta.domain.model.Movimiento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoRequest {
    @NotBlank
    private String numeroCuenta;
    
    @NotNull
    private Movimiento.TipoMovimiento tipoMovimiento;
    
    @NotNull
    @Positive
    private BigDecimal valor;
}
