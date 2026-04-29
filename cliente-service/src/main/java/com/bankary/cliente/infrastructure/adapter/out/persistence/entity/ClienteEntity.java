package com.bankary.cliente.infrastructure.adapter.out.persistence.entity;

import com.bankary.cliente.domain.model.TipoDocumento;
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

    @Enumerated(EnumType.STRING)
    @Column(table = "persona", name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(table = "persona", name = "numero_documento", nullable = false)
    private String numeroDocumento;

    @Column(table = "persona")
    private String direccion;

    @Column(table = "persona")
    private String telefono;

    @Column(nullable = false)
    private String contrasena;

    @Column(nullable = false)
    private boolean estado;
}
