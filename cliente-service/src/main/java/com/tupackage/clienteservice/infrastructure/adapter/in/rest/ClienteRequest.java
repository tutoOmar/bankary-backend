package com.tupackage.clienteservice.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de entrada para crear/actualizar un Cliente.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClienteRequest {

    @NotBlank(message = "Nombre es obligatorio")
    private String nombre;

    @NotNull(message = "Edad es obligatoria")
    @Min(value = 0, message = "Edad debe ser >= 0")
    private Integer edad;

    @NotBlank(message = "Identificacion es obligatoria")
    private String identificacion;

    /** Opcional en update. Si se envía, se re-hashea. */
    private String password;
}
