package com.bankary.cliente.infrastructure.adapter.out.persistence.entity;

import com.bankary.cliente.domain.model.TipoDocumento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "persona")
@SecondaryTable(name = "cliente", pkJoinColumns = @PrimaryKeyJoinColumn(name = "persona_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteEntity {

    @Id
    @Column(name = "persona_id")
    private UUID clienteId;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String genero;

    @Column
    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento", nullable = false)
    private String numeroDocumento;

    @Column
    private String direccion;

    @Column
    private String telefono;

    @Column(table = "cliente", nullable = false)
    private String contrasena;

    @Column(table = "cliente", nullable = false)
    private boolean estado;
}
