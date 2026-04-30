package com.bankary.cliente.application.dto;

import com.bankary.cliente.domain.model.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClienteCommand {
    private String nombre;
    private String genero;
    private Integer edad;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String direccion;
    private String telefono;
    private String contrasena;
}
