package com.bankary.cuenta.infrastructure.adapter.out.persistence.repository;

import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.MovimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MovimientoJpaRepository extends JpaRepository<MovimientoEntity, UUID> {
    List<MovimientoEntity> findByCuentaIdAndFechaBetween(UUID cuentaId, Instant start, Instant end);
}
