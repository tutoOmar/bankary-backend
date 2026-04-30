package com.bankary.cuenta.application.dto;

import com.bankary.cuenta.domain.model.TipoCuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaRequest {
    @NotBlank
    private String numeroCuenta;
    
    @NotNull
    private TipoCuenta tipoCuenta;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal saldoInicial;
    
    @NotNull
    private UUID clienteId;
}
