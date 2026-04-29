package com.bankary.cuenta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cuentas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaEntity {

    @Id
    private UUID id;

    @Column(name = "numero_cuenta", unique = true, nullable = false)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta")
    private TipoCuenta tipoCuenta;

    @Column(name = "saldo_inicial")
    private BigDecimal saldoInicial;

    @Column(name = "saldo_disponible")
    private BigDecimal saldoDisponible;

    private boolean estado;

    @Column(name = "cliente_id")
    private UUID clienteId;

    public enum TipoCuenta {
        AHORRO, CORRIENTE
    }
}
