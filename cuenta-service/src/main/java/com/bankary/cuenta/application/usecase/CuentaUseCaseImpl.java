package com.bankary.cuenta.application.usecase;

import com.bankary.cuenta.application.exception.ConflictException;
import com.bankary.cuenta.application.exception.ResourceNotFoundException;
import com.bankary.cuenta.domain.CuentaLimiteValidator;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.port.in.CuentaUseCase;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import com.bankary.cuenta.domain.port.out.ClienteSnapshotRepository;
import com.bankary.cuenta.domain.port.out.ClienteExternalServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuentaUseCaseImpl implements CuentaUseCase {

    private final CuentaRepository cuentaRepository;
    private final ClienteSnapshotRepository clienteSnapshotRepository;
    private final ClienteExternalServicePort clienteExternalServicePort;

    @Override
    public Cuenta create(Cuenta cuenta) {
        log.info("Iniciando creación de cuenta | numeroCuenta={} | clienteId={} | tipo={}", 
                cuenta.getNumeroCuenta(), cuenta.getClienteId(), cuenta.getTipoCuenta());
        
        cuentaRepository.findByNumeroCuenta(cuenta.getNumeroCuenta())
                .ifPresent(c -> {
                    log.error("Cuenta duplicada | numeroCuenta={}", cuenta.getNumeroCuenta());
                    throw new ConflictException("Ya existe una cuenta con el numero " + cuenta.getNumeroCuenta());
                });

        // Sincronización perezosa si el cliente no existe localmente
        if (clienteSnapshotRepository.findById(cuenta.getClienteId()).isEmpty()) {
            log.info("Cliente no encontrado localmente, consultando servicio externo | clienteId={}", cuenta.getClienteId());
            clienteExternalServicePort.findClienteById(cuenta.getClienteId())
                    .ifPresentOrElse(
                        snapshot -> {
                            log.info("Cliente encontrado en servicio externo, sincronizando snapshot | clienteId={}", cuenta.getClienteId());
                            clienteSnapshotRepository.save(snapshot);
                        },
                        () -> {
                            log.error("Cliente no encontrado en ningún servicio | clienteId={}", cuenta.getClienteId());
                            throw new ResourceNotFoundException("El cliente con ID " + cuenta.getClienteId() + " no existe en el sistema");
                        }
                    );
        }

        // Validar límite de cuentas por tipo
        List<Cuenta> cuentasActivas = cuentaRepository.findByClienteIdAndEstadoTrue(cuenta.getClienteId());
        try {
            CuentaLimiteValidator.validarLimitePorTipo(cuenta.getTipoCuenta(), cuentasActivas);
        } catch (Exception e) {
            log.error("Límite de cuentas excedido | clienteId={} | tipo={} | error={}", 
                    cuenta.getClienteId(), cuenta.getTipoCuenta(), e.getMessage());
            throw e;
        }

        if (cuenta.getId() == null) {
            cuenta.setId(UUID.randomUUID());
        }
        cuenta.setSaldoDisponible(cuenta.getSaldoInicial());
        cuenta.setEstado(true);
        
        Cuenta saved = cuentaRepository.save(cuenta);
        log.info("Cuenta creada exitosamente | id={} | numeroCuenta={}", saved.getId(), saved.getNumeroCuenta());
        return saved;
    }

    @Override
    public Cuenta update(String numeroCuenta, Cuenta cuenta) {
        log.info("Actualizando cuenta | numeroCuenta={}", numeroCuenta);
        Cuenta existing = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> {
                    log.warn("Cuenta no encontrada para actualización | numeroCuenta={}", numeroCuenta);
                    return new ResourceNotFoundException("Cuenta no encontrada");
                });
        
        existing.setTipoCuenta(cuenta.getTipoCuenta());
        existing.setEstado(cuenta.isEstado());
        
        Cuenta updated = cuentaRepository.save(existing);
        log.info("Cuenta actualizada exitosamente | id={}", updated.getId());
        return updated;
    }

    @Override
    public void delete(String numeroCuenta) {
        log.info("Eliminando cuenta | numeroCuenta={}", numeroCuenta);
        cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> {
                    log.warn("Cuenta no encontrada para eliminación | numeroCuenta={}", numeroCuenta);
                    return new ResourceNotFoundException("Cuenta no encontrada");
                });
        cuentaRepository.deleteByNumeroCuenta(numeroCuenta);
        log.info("Cuenta eliminada exitosamente | numeroCuenta={}", numeroCuenta);
    }

    @Override
    public Cuenta getByNumeroCuenta(String numeroCuenta) {
        log.debug("Consultando cuenta | numeroCuenta={}", numeroCuenta);
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> {
                    log.warn("Consulta fallida: cuenta no encontrada | numeroCuenta={}", numeroCuenta);
                    return new ResourceNotFoundException("Cuenta no encontrada");
                });
    }

    @Override
    public List<Cuenta> list() {
        log.debug("Listando todas las cuentas");
        return cuentaRepository.findAll();
    }
}
