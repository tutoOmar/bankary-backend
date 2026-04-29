package com.bankary.cliente.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "cliente")
@SecondaryTable(name = "persona", pkJoinColumns = @PrimaryKeyJoinColumn(name = "persona_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteEntity {

    @Id
    @Column(name = "persona_id")
    private UUID clienteId;

    @Column(table = "persona", nullable = false)
    private String nombre;

    @Column(table = "persona")
    private String genero;

    @Column(table = "persona")
    private Integer edad;

    @Column(table = "persona", unique = true, nullable = false)
    private String identificacion;

    @Column(table = "persona")
    private String direccion;

    @Column(table = "persona")
    private String telefono;

    @Column(nullable = false)
    private String contrasena;

    @Column(nullable = false)
    private boolean estado;
}
