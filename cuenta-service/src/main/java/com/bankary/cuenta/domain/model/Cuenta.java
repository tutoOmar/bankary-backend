package com.bankary.cuenta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {
    private UUID id;
    private String numeroCuenta;
    private TipoCuenta tipoCuenta;
    private BigDecimal saldoInicial;
    private BigDecimal saldoDisponible;
    private boolean estado;
    private UUID clienteId;

    public void initialize() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.saldoDisponible = this.saldoInicial;
        this.estado = true;
    }

    public void actualizarSaldo(BigDecimal nuevoSaldo) {
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }
        this.saldoDisponible = nuevoSaldo;
    }

    public void desactivar() {
        this.estado = false;
    }

    public void cambiarEstado(boolean estado) {
        this.estado = estado;
    }

    public void cambiarTipo(TipoCuenta tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }
}
