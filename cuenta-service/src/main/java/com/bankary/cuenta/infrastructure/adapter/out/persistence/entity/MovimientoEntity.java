package com.bankary.cuenta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento")
    private TipoMovimiento tipoMovimiento;

    private BigDecimal valor;
    private BigDecimal saldo;

    @Column(name = "cuenta_id")
    private UUID cuentaId;

    public enum TipoMovimiento {
        DEPOSITO, RETIRO
    }
}
