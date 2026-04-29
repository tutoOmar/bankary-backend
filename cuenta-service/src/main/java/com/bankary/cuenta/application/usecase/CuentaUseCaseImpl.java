package com.bankary.cuenta.application.usecase;

import com.bankary.cuenta.application.exception.ConflictException;
import com.bankary.cuenta.application.exception.ResourceNotFoundException;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.port.in.CuentaUseCase;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import com.bankary.cuenta.domain.port.out.ClienteSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CuentaUseCaseImpl implements CuentaUseCase {

    private final CuentaRepository cuentaRepository;
    private final ClienteSnapshotRepository clienteSnapshotRepository;

    @Override
    public Cuenta create(Cuenta cuenta) {
        cuentaRepository.findByNumeroCuenta(cuenta.getNumeroCuenta())
                .ifPresent(c -> {
                    throw new ConflictException("Ya existe una cuenta con el numero " + cuenta.getNumeroCuenta());
                });

        clienteSnapshotRepository.findById(cuenta.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("El cliente con ID " + cuenta.getClienteId() + " no existe o aun no ha sido sincronizado"));

        if (cuenta.getId() == null) {
            cuenta.setId(UUID.randomUUID());
        }
        cuenta.setSaldoDisponible(cuenta.getSaldoInicial());
        cuenta.setEstado(true);
        return cuentaRepository.save(cuenta);
    }

    @Override
    public Cuenta update(String numeroCuenta, Cuenta cuenta) {
        Cuenta existing = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
        
        existing.setTipoCuenta(cuenta.getTipoCuenta());
        existing.setEstado(cuenta.isEstado());
        // El saldo no se actualiza manualmente por regla de negocio (solo vía movimientos)
        
        return cuentaRepository.save(existing);
    }

    @Override
    public void delete(String numeroCuenta) {
        cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
        cuentaRepository.deleteByNumeroCuenta(numeroCuenta);
    }

    @Override
    public Cuenta getByNumeroCuenta(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
    }

    @Override
    public List<Cuenta> list() {
        return cuentaRepository.findAll();
    }
}
