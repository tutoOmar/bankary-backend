package com.bankary.cliente.application.usecase;

import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.dto.CreateClienteCommand;
import com.bankary.cliente.application.dto.UpdateClienteCommand;
import com.bankary.cliente.application.exception.ConflictException;
import com.bankary.cliente.application.exception.ResourceNotFoundException;
import com.bankary.cliente.application.mapper.ClienteMapper;
import com.bankary.cliente.domain.DocumentoValidator;
import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.port.in.ClienteCommandUseCase;
import com.bankary.cliente.domain.port.out.ClienteEventPublisher;
import com.bankary.cliente.domain.port.out.ClienteRepository;
import com.bankary.cliente.domain.port.out.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ClienteCommandUseCaseImpl implements ClienteCommandUseCase {

    private final ClienteRepository clienteRepository;
    private final ClienteEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ClienteResponse create(CreateClienteCommand command) {
        log.info("Iniciando creación de cliente | tipoDocumento={} | numeroDocumento={}", 
                command.getTipoDocumento(), command.getNumeroDocumento());
        DocumentoValidator.validate(command.getTipoDocumento(), command.getNumeroDocumento(), command.getEdad());

        Optional<Cliente> existing = clienteRepository.findByDocumento(command.getTipoDocumento(), command.getNumeroDocumento());
        if (existing.isPresent()) {
            log.error("Cliente duplicado detectado | tipoDocumento={} | numeroDocumento={}", 
                    command.getTipoDocumento(), command.getNumeroDocumento());
            throw new ConflictException("Ya existe un cliente con el documento dado");
        }

        Cliente cliente = new Cliente();
        cliente.setClienteId(UUID.randomUUID());
        cliente.setNombre(command.getNombre());
        cliente.setGenero(command.getGenero());
        cliente.setEdad(command.getEdad());
        cliente.setTipoDocumento(command.getTipoDocumento());
        cliente.setNumeroDocumento(command.getNumeroDocumento());
        cliente.setDireccion(command.getDireccion());
        cliente.setTelefono(command.getTelefono());
        cliente.setContrasena(passwordEncoder.encode(command.getContrasena()));
        cliente.setEstado(true);

        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente persistido exitosamente | id={} | nombre={}", saved.getClienteId(), saved.getNombre());

        try {
            eventPublisher.publishClienteCreado(
                    new ClienteEventPublisher.ClienteEvent(saved.getClienteId(), saved.getNombre(), Instant.now(),
                            "cliente.created"));
        } catch (Exception e) {
            log.error("Fallo crítico al publicar evento RabbitMQ (cliente.created) | id={} | error={}", 
                    saved.getClienteId(), e.getMessage());
        }

        return ClienteMapper.toResponse(saved);
    }

    @Override
    public ClienteResponse update(UUID clienteId, UpdateClienteCommand command) {
        log.info("Actualizando cliente | id={}", clienteId);
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado para actualización | id={}", clienteId);
                    return new ResourceNotFoundException("Cliente no encontrado");
                });

        DocumentoValidator.validate(command.getTipoDocumento(), command.getNumeroDocumento(), command.getEdad());

        if (!cliente.getTipoDocumento().equals(command.getTipoDocumento()) || 
            !cliente.getNumeroDocumento().equals(command.getNumeroDocumento())) {
            Optional<Cliente> existing = clienteRepository.findByDocumento(command.getTipoDocumento(), command.getNumeroDocumento());
            if (existing.isPresent() && !existing.get().getClienteId().equals(clienteId)) {
                log.error("Conflicto en actualización de documento | id={} | nuevoTipo={} | nuevoNumero={}", 
                        clienteId, command.getTipoDocumento(), command.getNumeroDocumento());
                throw new ConflictException("Ya existe otro cliente con el documento dado");
            }
        }

        cliente.setNombre(command.getNombre());
        cliente.setGenero(command.getGenero());
        cliente.setEdad(command.getEdad());
        cliente.setTipoDocumento(command.getTipoDocumento());
        cliente.setNumeroDocumento(command.getNumeroDocumento());
        cliente.setDireccion(command.getDireccion());
        cliente.setTelefono(command.getTelefono());
        if (command.getContrasena() != null && !command.getContrasena().isBlank()) {
            cliente.setContrasena(passwordEncoder.encode(command.getContrasena()));
        }

        Cliente updated = clienteRepository.save(cliente);
        log.info("Cliente actualizado exitosamente | id={}", updated.getClienteId());

        try {
            eventPublisher.publishClienteActualizado(
                    new ClienteEventPublisher.ClienteEvent(updated.getClienteId(), updated.getNombre(), Instant.now(),
                            "cliente.updated"));
        } catch (Exception e) {
            log.error("Fallo crítico al publicar evento RabbitMQ (cliente.updated) | id={} | error={}", 
                    updated.getClienteId(), e.getMessage());
        }

        return ClienteMapper.toResponse(updated);
    }

    @Override
    public void delete(UUID clienteId) {
        log.info("Eliminando cliente (soft delete) | id={}", clienteId);
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> {
                    log.warn("Cliente no encontrado para eliminación | id={}", clienteId);
                    return new ResourceNotFoundException("Cliente no encontrado");
                });
        cliente.setEstado(false);
        clienteRepository.save(cliente);
        log.info("Cliente marcado como inactivo | id={}", clienteId);
    }
}
