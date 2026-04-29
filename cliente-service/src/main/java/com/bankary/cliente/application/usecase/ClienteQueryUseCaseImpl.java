package com.bankary.cliente.application.usecase;

import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.exception.ResourceNotFoundException;
import com.bankary.cliente.application.mapper.ClienteMapper;
import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.port.in.ClienteQueryUseCase;
import com.bankary.cliente.domain.port.out.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ClienteQueryUseCaseImpl implements ClienteQueryUseCase {

    private final ClienteRepository clienteRepository;

    @Override
    public ClienteResponse getById(UUID clienteId) {
        log.debug("Consultando cliente por id={}", clienteId);
        Cliente cliente = clienteRepository.findById(clienteId)
                .filter(Cliente::isEstado)
                .orElseThrow(() -> {
                    log.warn("Consulta fallida: cliente no encontrado o inactivo | id={}", clienteId);
                    return new ResourceNotFoundException("Cliente no encontrado");
                });
        return ClienteMapper.toResponse(cliente);
    }

    @Override
    public List<ClienteResponse> list() {
        log.debug("Listando todos los clientes activos");
        return clienteRepository.findAllActivos().stream()
                .map(ClienteMapper::toResponse)
                .collect(Collectors.toList());
    }
}
