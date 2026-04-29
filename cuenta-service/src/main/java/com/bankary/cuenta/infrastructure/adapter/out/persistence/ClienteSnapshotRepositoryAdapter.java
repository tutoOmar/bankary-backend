package com.bankary.cuenta.infrastructure.adapter.out.persistence;

import com.bankary.cuenta.domain.model.ClienteSnapshot;
import com.bankary.cuenta.domain.port.out.ClienteSnapshotRepository;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.ClienteSnapshotEntity;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.repository.ClienteSnapshotJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClienteSnapshotRepositoryAdapter implements ClienteSnapshotRepository {

    private final ClienteSnapshotJpaRepository jpaRepository;

    @Override
    public ClienteSnapshot save(ClienteSnapshot snapshot) {
        ClienteSnapshotEntity entity = ClienteSnapshotEntity.builder()
                .clienteId(snapshot.getClienteId())
                .nombre(snapshot.getNombre())
                .lastUpdated(snapshot.getLastUpdated())
                .build();
        ClienteSnapshotEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ClienteSnapshot> findById(UUID clienteId) {
        return jpaRepository.findById(clienteId).map(this::toDomain);
    }

    private ClienteSnapshot toDomain(ClienteSnapshotEntity entity) {
        return ClienteSnapshot.builder()
                .clienteId(entity.getClienteId())
                .nombre(entity.getNombre())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
}
