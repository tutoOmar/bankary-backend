package com.bankary.cliente.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Persona {
    private String nombre;
    private String genero;
    private Integer edad;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String direccion;
    private String telefono;

    public void validate() {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (edad == null || edad < 0 || edad > 120) {
            throw new IllegalArgumentException("La edad debe estar entre 0 y 120");
        }
        if (tipoDocumento == null) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio");
        }
        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new IllegalArgumentException("El numero de documento es obligatorio");
        }
    }
}
