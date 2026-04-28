package com.tupackage.clienteservice.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ClienteRequest {
    @NotBlank
    private String nombre;
    @NotNull
    private Integer edad;
    @NotBlank
    private String identificacion;
    private String password;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
