package com.bankary.cuenta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cliente_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteSnapshotEntity {

    @Id
    @Column(name = "cliente_id")
    private UUID clienteId;

    private String nombre;

    @Column(name = "last_updated")
    private Instant lastUpdated;
}
