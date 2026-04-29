package com.bankary.cuenta.infrastructure.adapter.out.persistence.repository;

import com.bankary.cuenta.infrastructure.adapter.out.persistence.entity.CuentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CuentaJpaRepository extends JpaRepository<CuentaEntity, UUID> {
    Optional<CuentaEntity> findByNumeroCuenta(String numeroCuenta);
    void deleteByNumeroCuenta(String numeroCuenta);
}
