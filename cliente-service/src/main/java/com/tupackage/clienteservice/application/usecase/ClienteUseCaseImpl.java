package com.tupackage.clienteservice.application.usecase;

import com.tupackage.clienteservice.domain.model.Cliente;
import com.tupackage.clienteservice.domain.port.in.ClienteUseCase;
import com.tupackage.clienteservice.domain.port.out.ClienteEventPublisher;
import com.tupackage.clienteservice.domain.port.out.ClienteRepository;
import com.tupackage.clienteservice.infrastructure.adapter.out.messaging.ClienteEventMessage;
import com.tupackage.clienteservice.exception.ClienteNotFoundException;
import com.tupackage.clienteservice.exception.DuplicateIdentificacionException;
import com.tupackage.clienteservice.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClienteUseCaseImpl implements ClienteUseCase {
    private static final Logger log = LoggerFactory.getLogger(ClienteUseCaseImpl.class);

    private final ClienteRepository clienteRepository;
    private final ClienteEventPublisher eventPublisher;
    private final BCryptPasswordEncoder passwordEncoder;

    public ClienteUseCaseImpl(ClienteRepository clienteRepository,
                              ClienteEventPublisher eventPublisher,
                              BCryptPasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Cliente create(Cliente cliente) {
        validateForCreate(cliente);

        if (cliente.getPassword() != null) {
            cliente.setPasswordHash(passwordEncoder.encode(cliente.getPassword()));
            cliente.setPassword(null);
        }

        Cliente saved = clienteRepository.save(cliente);

        // Publish event fire-and-forget
        ClienteEventMessage event = new ClienteEventMessage(saved.getId(), saved.getNombre(), Instant.now(), "CLIENTE_CREATED");
        try {
            eventPublisher.publish(event);
        } catch (Exception e) {
            log.error("Failed to publish cliente.created event for id {}", saved.getId(), e);
        }

        return saved;
    }

    @Override
    @Transactional
    public Cliente update(UUID id, Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new ValidationException("Nombre obligatorio");
        }
        if (cliente.getEdad() != null && cliente.getEdad() < 0) {
            throw new ValidationException("Edad debe ser >= 0");
        }

        Cliente existing = clienteRepository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));

        Optional<Cliente> byIdent = clienteRepository.findByIdentificacion(cliente.getIdentificacion());
        if (byIdent.isPresent() && !byIdent.get().getId().equals(id) && byIdent.get().isEstado()) {
            throw new DuplicateIdentificacionException("Identificacion ya existe para otro cliente activo");
        }

        existing.setNombre(cliente.getNombre());
        existing.setEdad(cliente.getEdad());
        existing.setIdentificacion(cliente.getIdentificacion());
        if (cliente.getPassword() != null) {
            existing.setPasswordHash(passwordEncoder.encode(cliente.getPassword()));
        }

        Cliente saved = clienteRepository.save(existing);

        ClienteEventMessage event = new ClienteEventMessage(saved.getId(), saved.getNombre(), Instant.now(), "CLIENTE_UPDATED");
        try {
            eventPublisher.publish(event);
        } catch (Exception e) {
            log.error("Failed to publish cliente.updated event for id {}", saved.getId(), e);
        }

        return saved;
    }

    @Override
    public Cliente findById(UUID id) {
        return clienteRepository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
    }

    @Override
    public List<Cliente> findAllActive() {
        return clienteRepository.findAllActive();
    }

    @Override
    @Transactional
    public Cliente deleteLogical(UUID id) {
        Cliente existing = clienteRepository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
        existing.setEstado(false);
        Cliente saved = clienteRepository.save(existing);

        ClienteEventMessage event = new ClienteEventMessage(saved.getId(), saved.getNombre(), Instant.now(), "CLIENTE_DELETED");
        try {
            eventPublisher.publish(event);
        } catch (Exception e) {
            log.error("Failed to publish cliente.deleted event for id {}", saved.getId(), e);
        }

        return saved;
    }

    private void validateForCreate(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new ValidationException("Nombre obligatorio");
        }
        if (cliente.getEdad() != null && cliente.getEdad() < 0) {
            throw new ValidationException("Edad debe ser >= 0");
        }
        if (cliente.getIdentificacion() == null || cliente.getIdentificacion().trim().isEmpty()) {
            throw new ValidationException("Identificacion obligatoria");
        }

        Optional<Cliente> existing = clienteRepository.findByIdentificacion(cliente.getIdentificacion());
        if (existing.isPresent() && existing.get().isEstado()) {
            throw new DuplicateIdentificacionException("Identificacion ya existe para un cliente activo");
        }
    }
}
