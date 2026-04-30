package com.bankary.cuenta.infrastructure.adapter.out.persistence;

import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.TipoCuenta;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.CuentaEntity;
import com.bankary.cuenta.infrastructure.adapter.out.persistence.repository.CuentaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CuentaRepositoryAdapter implements CuentaRepository {

    private final CuentaJpaRepository jpaRepository;

    @Override
    public Cuenta save(Cuenta cuenta) {
        CuentaEntity entity = toEntity(cuenta);
        CuentaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Cuenta> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Cuenta> findByNumeroCuenta(String numeroCuenta) {
        return jpaRepository.findByNumeroCuenta(numeroCuenta).map(this::toDomain);
    }

    @Override
    public List<Cuenta> findByClienteIdAndEstadoTrue(UUID clienteId) {
        return jpaRepository.findByClienteIdAndEstadoTrue(clienteId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Cuenta> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByNumeroCuenta(String numeroCuenta) {
        jpaRepository.deleteByNumeroCuenta(numeroCuenta);
    }

    private Cuenta toDomain(CuentaEntity entity) {
        return Cuenta.builder()
                .id(entity.getId())
                .numeroCuenta(entity.getNumeroCuenta())
                .tipoCuenta(TipoCuenta.valueOf(entity.getTipoCuenta().name()))
                .saldoInicial(entity.getSaldoInicial())
                .saldoDisponible(entity.getSaldoDisponible())
                .estado(entity.isEstado())
                .clienteId(entity.getClienteId())
                .build();
    }

    private CuentaEntity toEntity(Cuenta domain) {
        return CuentaEntity.builder()
                .id(domain.getId())
                .numeroCuenta(domain.getNumeroCuenta())
                .tipoCuenta(CuentaEntity.TipoCuenta.valueOf(domain.getTipoCuenta().name()))
                .saldoInicial(domain.getSaldoInicial())
                .saldoDisponible(domain.getSaldoDisponible())
                .estado(domain.isEstado())
                .clienteId(domain.getClienteId())
                .build();
    }
}
