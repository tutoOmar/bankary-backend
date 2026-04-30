package com.bankary.cliente.infrastructure.adapter.out.persistence.repository;

import com.bankary.cliente.domain.model.TipoDocumento;
import com.bankary.cliente.infrastructure.adapter.out.persistence.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaClienteRepository extends JpaRepository<ClienteEntity, UUID> {
    Optional<ClienteEntity> findByTipoDocumentoAndNumeroDocumento(TipoDocumento tipoDocumento, String numeroDocumento);
    java.util.List<ClienteEntity> findByEstadoTrue();
}
