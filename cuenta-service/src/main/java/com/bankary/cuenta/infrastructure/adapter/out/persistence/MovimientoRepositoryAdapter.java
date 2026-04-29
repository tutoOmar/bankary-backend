package com.bankary.cuenta.infrastructure.adapter.out.persistence;

import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.domain.port.out.MovimientoRepository;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.MovimientoEntity;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.repository.MovimientoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MovimientoRepositoryAdapter implements MovimientoRepository {

    private final MovimientoJpaRepository jpaRepository;

    @Override
    public Movimiento save(Movimiento movimiento) {
        MovimientoEntity entity = toEntity(movimiento);
        MovimientoEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Movimiento> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Movimiento> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Movimiento> findByCuentaIdAndFechaBetween(UUID cuentaId, Instant start, Instant end) {
        return jpaRepository.findByCuentaIdAndFechaBetween(cuentaId, start, end).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Movimiento toDomain(MovimientoEntity entity) {
        return Movimiento.builder()
                .id(entity.getId())
                .fecha(entity.getFecha())
                .tipoMovimiento(Movimiento.TipoMovimiento.valueOf(entity.getTipoMovimiento().name()))
                .valor(entity.getValor())
                .saldo(entity.getSaldo())
                .cuentaId(entity.getCuentaId())
                .build();
    }

    private MovimientoEntity toEntity(Movimiento domain) {
        return MovimientoEntity.builder()
                .id(domain.getId())
                .fecha(domain.getFecha())
                .tipoMovimiento(MovimientoEntity.TipoMovimiento.valueOf(domain.getTipoMovimiento().name()))
                .valor(domain.getValor())
                .saldo(domain.getSaldo())
                .cuentaId(domain.getCuentaId())
                .build();
    }
}
