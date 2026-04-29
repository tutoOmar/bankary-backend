package com.bankary.cuenta.infrastructure.adapter.out.persistence.repository;

import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.ClienteSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClienteSnapshotJpaRepository extends JpaRepository<ClienteSnapshotEntity, UUID> {
}
