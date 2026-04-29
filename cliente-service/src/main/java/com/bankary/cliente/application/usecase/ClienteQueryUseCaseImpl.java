package com.bankary.cliente.application.usecase;

import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.exception.ResourceNotFoundException;
import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.port.in.ClienteQueryUseCase;
import com.bankary.cliente.domain.port.out.ClienteRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ClienteQueryUseCaseImpl implements ClienteQueryUseCase {

    private final ClienteRepository clienteRepository;

    @Override
    public ClienteResponse getById(UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .filter(Cliente::isEstado)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        return toResponse(cliente);
    }

    @Override
    public List<ClienteResponse> list() {
        return clienteRepository.findAllActivos().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .genero(cliente.getGenero())
                .edad(cliente.getEdad())
                .identificacion(cliente.getIdentificacion())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .estado(cliente.isEstado())
                .build();
    }
}
