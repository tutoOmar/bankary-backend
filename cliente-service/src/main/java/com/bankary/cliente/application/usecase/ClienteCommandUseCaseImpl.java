package com.bankary.cliente.application.usecase;

import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.dto.CreateClienteCommand;
import com.bankary.cliente.application.dto.UpdateClienteCommand;
import com.bankary.cliente.application.exception.ConflictException;
import com.bankary.cliente.application.exception.ResourceNotFoundException;
import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.port.in.ClienteCommandUseCase;
import com.bankary.cliente.domain.port.out.ClienteEventPublisher;
import com.bankary.cliente.domain.port.out.ClienteRepository;
import com.bankary.cliente.domain.port.out.PasswordEncoder;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ClienteCommandUseCaseImpl implements ClienteCommandUseCase {

    private final ClienteRepository clienteRepository;
    private final ClienteEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ClienteResponse create(CreateClienteCommand command) {
        Optional<Cliente> existing = clienteRepository.findByIdentificacion(command.getIdentificacion());
        if (existing.isPresent()) {
            throw new ConflictException("Ya existe un cliente con la identificacion dada");
        }

        Cliente cliente = new Cliente();
        cliente.setClienteId(UUID.randomUUID());
        cliente.setNombre(command.getNombre());
        cliente.setGenero(command.getGenero());
        cliente.setEdad(command.getEdad());
        cliente.setIdentificacion(command.getIdentificacion());
        cliente.setDireccion(command.getDireccion());
        cliente.setTelefono(command.getTelefono());
        cliente.setContrasena(passwordEncoder.encode(command.getContrasena()));
        cliente.setEstado(true);

        Cliente saved = clienteRepository.save(cliente);

        try {
            eventPublisher.publishClienteCreado(
                    new ClienteEventPublisher.ClienteEvent(saved.getClienteId(), saved.getNombre(), Instant.now(),
                            "cliente.created"));
        } catch (Exception e) {
            // Log and ignore to not fail transaction
        }

        return toResponse(saved);
    }

    @Override
    public ClienteResponse update(UUID clienteId, UpdateClienteCommand command) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (!cliente.getIdentificacion().equals(command.getIdentificacion())) {
            Optional<Cliente> existing = clienteRepository.findByIdentificacion(command.getIdentificacion());
            if (existing.isPresent() && !existing.get().getClienteId().equals(clienteId)) {
                throw new ConflictException("Ya existe otro cliente con la identificacion dada");
            }
        }

        cliente.setNombre(command.getNombre());
        cliente.setGenero(command.getGenero());
        cliente.setEdad(command.getEdad());
        cliente.setIdentificacion(command.getIdentificacion());
        cliente.setDireccion(command.getDireccion());
        cliente.setTelefono(command.getTelefono());
        if (command.getContrasena() != null && !command.getContrasena().isBlank()) {
            cliente.setContrasena(passwordEncoder.encode(command.getContrasena()));
        }

        Cliente updated = clienteRepository.save(cliente);

        try {
            eventPublisher.publishClienteActualizado(
                    new ClienteEventPublisher.ClienteEvent(updated.getClienteId(), updated.getNombre(), Instant.now(),
                            "cliente.updated"));
        } catch (Exception e) {
            // Log and ignore
        }

        return toResponse(updated);
    }

    @Override
    public void delete(UUID clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        cliente.setEstado(false);
        clienteRepository.save(cliente);
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
