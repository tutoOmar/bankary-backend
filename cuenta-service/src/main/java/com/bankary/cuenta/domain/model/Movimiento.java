package com.bankary.cuenta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
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

    public void registrar(BigDecimal nuevoSaldo, UUID cuentaId) {
        this.fecha = Instant.now();
        this.saldo = nuevoSaldo;
        this.cuentaId = cuentaId;
    }

    public enum TipoMovimiento {
        DEPOSITO, RETIRO
    }
}
