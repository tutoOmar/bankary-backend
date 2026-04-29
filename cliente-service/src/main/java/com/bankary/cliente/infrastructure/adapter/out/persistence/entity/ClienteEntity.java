package com.bankary.cliente.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteEntity {

    @Id
    @Column(name = "cliente_id")
    private UUID clienteId;

    private String nombre;
    private String genero;
    private Integer edad;

    @Column(unique = true)
    private String identificacion;

    private String direccion;
    private String telefono;
    private String contrasena;
    private boolean estado;
}
