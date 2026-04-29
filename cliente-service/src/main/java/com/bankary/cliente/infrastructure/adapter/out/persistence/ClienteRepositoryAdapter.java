package com.bankary.cliente.infrastructure.adapter.out.persistence;

import com.bankary.cliente.domain.model.Cliente;
import com.bankary.cliente.domain.model.TipoDocumento;
import com.bankary.cliente.domain.port.out.ClienteRepository;
import com.bankary.cliente.infrastructure.adapter.out.persistence.entity.ClienteEntity;
import com.bankary.cliente.infrastructure.adapter.out.persistence.repository.JpaClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final JpaClienteRepository jpaRepository;

    @Override
    public Optional<Cliente> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Cliente> findByDocumento(TipoDocumento tipoDocumento, String numeroDocumento) {
        return jpaRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento).map(this::toDomain);
    }

    @Override
    public List<Cliente> findAllActivos() {
        return jpaRepository.findByEstadoTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Cliente save(Cliente cliente) {
        ClienteEntity entity = toEntity(cliente);
        ClienteEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private Cliente toDomain(ClienteEntity entity) {
        return Cliente.builder()
                .clienteId(entity.getClienteId())
                .nombre(entity.getNombre())
                .genero(entity.getGenero())
                .edad(entity.getEdad())
                .tipoDocumento(entity.getTipoDocumento())
                .numeroDocumento(entity.getNumeroDocumento())
                .direccion(entity.getDireccion())
                .telefono(entity.getTelefono())
                .contrasena(entity.getContrasena())
                .estado(entity.isEstado())
                .build();
    }

    private ClienteEntity toEntity(Cliente domain) {
        return ClienteEntity.builder()
                .clienteId(domain.getClienteId())
                .nombre(domain.getNombre())
                .genero(domain.getGenero())
                .edad(domain.getEdad())
                .tipoDocumento(domain.getTipoDocumento())
                .numeroDocumento(domain.getNumeroDocumento())
                .direccion(domain.getDireccion())
                .telefono(domain.getTelefono())
                .contrasena(domain.getContrasena())
                .estado(domain.isEstado())
                .build();
    }
}
