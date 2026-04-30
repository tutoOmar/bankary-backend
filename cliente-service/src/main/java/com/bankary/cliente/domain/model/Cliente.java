package com.bankary.cliente.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Cliente extends Persona {
    private UUID clienteId;
    private String contrasena;
    private boolean estado;

    public void updateDetails(String nombre, String genero, Integer edad, TipoDocumento tipoDocumento, String numeroDocumento, String direccion, String telefono, String contrasena) {
        super.updateInfo(nombre, genero, edad, tipoDocumento, numeroDocumento, direccion, telefono);
        if (contrasena != null && !contrasena.isBlank()) {
            this.contrasena = contrasena;
        }
    }

    public void deactivate() {
        this.estado = false;
    }
}
