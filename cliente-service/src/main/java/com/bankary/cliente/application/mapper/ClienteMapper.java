package com.bankary.cliente.application.mapper;

import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.domain.model.Cliente;

public class ClienteMapper {
    
    public static ClienteResponse toResponse(Cliente cliente) {
        if (cliente == null) return null;
        
        return ClienteResponse.builder()
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .genero(cliente.getGenero())
                .edad(cliente.getEdad())
                .tipoDocumento(cliente.getTipoDocumento() != null ? cliente.getTipoDocumento().name() : null)
                .numeroDocumento(cliente.getNumeroDocumento())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .estado(cliente.isEstado())
                .build();
    }
}
