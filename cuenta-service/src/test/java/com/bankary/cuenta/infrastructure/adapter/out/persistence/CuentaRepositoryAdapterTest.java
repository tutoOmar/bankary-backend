package com.bankary.cuenta.infrastructure.adapter.out.persistence;

import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.TipoCuenta;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.CuentaEntity;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.repository.CuentaJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CuentaRepositoryAdapter Unit Tests")
class CuentaRepositoryAdapterTest {

    @Mock
    private CuentaJpaRepository jpaRepository;

    @InjectMocks
    private CuentaRepositoryAdapter adapter;

    @Test
    @DisplayName("save - Success")
    void save_Exitoso() {
        Cuenta domain = Cuenta.builder().numeroCuenta("123").tipoCuenta(TipoCuenta.AHORRO).saldoInicial(BigDecimal.ZERO).saldoDisponible(BigDecimal.ZERO).build();
        CuentaEntity entity = CuentaEntity.builder().numeroCuenta("123").tipoCuenta(CuentaEntity.TipoCuenta.AHORRO).saldoInicial(BigDecimal.ZERO).saldoDisponible(BigDecimal.ZERO).build();
        when(jpaRepository.save(any())).thenReturn(entity);

        Cuenta result = adapter.save(domain);

        assertEquals("123", result.getNumeroCuenta());
    }

    @Test
    @DisplayName("findById - Success")
    void findById_Exitoso() {
        UUID id = UUID.randomUUID();
        CuentaEntity entity = CuentaEntity.builder().id(id).numeroCuenta("123").tipoCuenta(CuentaEntity.TipoCuenta.AHORRO).saldoInicial(BigDecimal.ZERO).saldoDisponible(BigDecimal.ZERO).build();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        Optional<Cuenta> result = adapter.findById(id);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("findByNumeroCuenta - Success")
    void findByNumeroCuenta_Exitoso() {
        CuentaEntity entity = CuentaEntity.builder().numeroCuenta("123").tipoCuenta(CuentaEntity.TipoCuenta.AHORRO).saldoInicial(BigDecimal.ZERO).saldoDisponible(BigDecimal.ZERO).build();
        when(jpaRepository.findByNumeroCuenta("123")).thenReturn(Optional.of(entity));

        Optional<Cuenta> result = adapter.findByNumeroCuenta("123");

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("findAll - Success")
    void findAll_Exitoso() {
        when(jpaRepository.findAll()).thenReturn(List.of(CuentaEntity.builder().tipoCuenta(CuentaEntity.TipoCuenta.AHORRO).saldoInicial(BigDecimal.ZERO).saldoDisponible(BigDecimal.ZERO).build()));

        List<Cuenta> result = adapter.findAll();

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("deleteByNumeroCuenta - Success")
    void deleteByNumero_Exitoso() {
        adapter.deleteByNumeroCuenta("123");
        verify(jpaRepository).deleteByNumeroCuenta("123");
    }
}
