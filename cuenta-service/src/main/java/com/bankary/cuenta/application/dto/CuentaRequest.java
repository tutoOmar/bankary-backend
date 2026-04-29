package com.bankary.cuenta.application.dto;

import com.bankary.cuenta.domain.model.Cuenta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CuentaRequest {
    @NotBlank
    private String numeroCuenta;
    
    @NotNull
    private Cuenta.TipoCuenta tipoCuenta;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal saldoInicial;
    
    @NotNull
    private UUID clienteId;
}
