package com.bankary.cuenta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimiento {
    private UUID id;
    private Instant fecha;
    private TipoMovimiento tipoMovimiento;
    private BigDecimal valor;
    private BigDecimal saldo;
    private UUID cuentaId;

    public enum TipoMovimiento {
        DEPOSITO, RETIRO
    }
}
