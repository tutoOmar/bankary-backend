package com.bankary.cuenta.infrastructure.adapter.out.persistence;

import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.MovimientoEntity;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.repository.MovimientoJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovimientoRepositoryAdapter Unit Tests")
class MovimientoRepositoryAdapterTest {

    @Mock
    private MovimientoJpaRepository jpaRepository;

    @InjectMocks
    private MovimientoRepositoryAdapter adapter;

    @Test
    @DisplayName("save - Success")
    void save_Exitoso() {
        Movimiento domain = Movimiento.builder().tipoMovimiento(Movimiento.TipoMovimiento.DEPOSITO).valor(BigDecimal.ONE).build();
        MovimientoEntity entity = MovimientoEntity.builder().tipoMovimiento(MovimientoEntity.TipoMovimiento.DEPOSITO).valor(BigDecimal.ONE).build();
        when(jpaRepository.save(any())).thenReturn(entity);

        Movimiento result = adapter.save(domain);

        assertNotNull(result);
    }

    @Test
    @DisplayName("findAll - Success")
    void findAll_Exitoso() {
        when(jpaRepository.findAll()).thenReturn(List.of(MovimientoEntity.builder().tipoMovimiento(MovimientoEntity.TipoMovimiento.DEPOSITO).valor(BigDecimal.ONE).build()));

        List<Movimiento> result = adapter.findAll();

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("findById - Success")
    void findById_Exitoso() {
        UUID id = UUID.randomUUID();
        MovimientoEntity entity = MovimientoEntity.builder().id(id).tipoMovimiento(MovimientoEntity.TipoMovimiento.DEPOSITO).valor(BigDecimal.ONE).build();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Movimiento> result = adapter.findById(id);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("findByCuentaIdAndFechaBetween - Success")
    void findByCuentaIdAndFechaBetween_Exitoso() {
        UUID cuentaId = UUID.randomUUID();
        Instant now = Instant.now();
        when(jpaRepository.findByCuentaIdAndFechaBetween(any(), any(), any())).thenReturn(List.of(MovimientoEntity.builder().tipoMovimiento(MovimientoEntity.TipoMovimiento.DEPOSITO).valor(BigDecimal.ONE).build()));

        List<Movimiento> result = adapter.findByCuentaIdAndFechaBetween(cuentaId, now, now);

        assertFalse(result.isEmpty());
    }
}
