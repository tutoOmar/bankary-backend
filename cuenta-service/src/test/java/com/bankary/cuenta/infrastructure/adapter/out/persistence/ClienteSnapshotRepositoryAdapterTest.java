package com.bankary.cuenta.infrastructure.adapter.out.persistence;

import com.bankary.cuenta.domain.model.ClienteSnapshot;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.ClienteSnapshotEntity;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.repository.ClienteSnapshotJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteSnapshotRepositoryAdapter Unit Tests")
class ClienteSnapshotRepositoryAdapterTest {

    @Mock
    private ClienteSnapshotJpaRepository jpaRepository;

    @InjectMocks
    private ClienteSnapshotRepositoryAdapter adapter;

    @Test
    @DisplayName("save - Success")
    void save_Exitoso() {
        UUID id = UUID.randomUUID();
        ClienteSnapshot domain = ClienteSnapshot.builder().clienteId(id).nombre("Test").lastUpdated(Instant.now()).build();
        ClienteSnapshotEntity entity = ClienteSnapshotEntity.builder().clienteId(id).nombre("Test").lastUpdated(Instant.now()).build();
        when(jpaRepository.save(any())).thenReturn(entity);

        ClienteSnapshot result = adapter.save(domain);

        assertEquals("Test", result.getNombre());
    }

    @Test
    @DisplayName("findById - Success")
    void findById_Exitoso() {
        UUID id = UUID.randomUUID();
        ClienteSnapshotEntity entity = ClienteSnapshotEntity.builder().clienteId(id).nombre("Test").lastUpdated(Instant.now()).build();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<ClienteSnapshot> result = adapter.findById(id);

        assertTrue(result.isPresent());
    }
}
