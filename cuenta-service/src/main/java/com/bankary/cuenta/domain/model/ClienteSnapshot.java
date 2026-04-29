package com.bankary.cuenta.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteSnapshot {
    private UUID clienteId;
    private String nombre;
    private Instant lastUpdated;
}
