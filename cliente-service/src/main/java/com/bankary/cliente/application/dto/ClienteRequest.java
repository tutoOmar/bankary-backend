package com.bankary.cliente.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @NotBlank(message = "El genero es obligatorio")
    private String genero;
    
    @NotNull(message = "La edad es obligatoria")
    @PositiveOrZero(message = "La edad debe ser mayor o igual a 0")
    private Integer edad;
    
    @NotBlank(message = "La identificacion es obligatoria")
    private String identificacion;
    
    @NotBlank(message = "La direccion es obligatoria")
    private String direccion;
    
    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;
    
    private String contrasena;
}
