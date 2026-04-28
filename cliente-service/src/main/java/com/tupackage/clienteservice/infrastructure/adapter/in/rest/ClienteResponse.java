package com.tupackage.clienteservice.infrastructure.adapter.in.rest;

import com.tupackage.clienteservice.domain.model.Cliente;

import java.util.UUID;

public class ClienteResponse {
    private UUID id;
    private String nombre;
    private Integer edad;
    private String identificacion;
    private boolean estado;

    public static ClienteResponse fromDomain(Cliente c) {
        ClienteResponse r = new ClienteResponse();
        r.id = c.getId();
        r.nombre = c.getNombre();
        r.edad = c.getEdad();
        r.identificacion = c.getIdentificacion();
        r.estado = c.isEstado();
        return r;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public Integer getEdad() { return edad; }
    public String getIdentificacion() { return identificacion; }
    public boolean isEstado() { return estado; }
}
