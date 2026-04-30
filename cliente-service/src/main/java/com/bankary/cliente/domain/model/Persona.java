package com.bankary.cliente.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@EqualsAndHashCode
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

    public void updateInfo(String nombre, String genero, Integer edad, TipoDocumento tipoDocumento, String numeroDocumento, String direccion, String telefono) {
        this.nombre = nombre;
        this.genero = genero;
        this.edad = edad;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.direccion = direccion;
        this.telefono = telefono;
        validate();
    }
}
