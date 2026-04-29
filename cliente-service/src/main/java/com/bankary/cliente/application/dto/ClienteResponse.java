package com.bankary.cliente.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private UUID clienteId;
    private String nombre;
    private String genero;
    private Integer edad;
    private String tipoDocumento;
    private String numeroDocumento;
    private String direccion;
    private String telefono;
    private boolean estado;
}
