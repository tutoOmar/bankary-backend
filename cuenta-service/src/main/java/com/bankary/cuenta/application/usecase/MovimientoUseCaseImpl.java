package com.bankary.cuenta.application.usecase;

import com.bankary.cuenta.application.exception.ResourceNotFoundException;
import com.bankary.cuenta.application.exception.SaldoInsuficienteException;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.domain.port.in.MovimientoUseCase;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import com.bankary.cuenta.domain.port.out.MovimientoRepository;
import com.bankary.cuenta.domain.model.strategy.CuentaStrategy;
import com.bankary.cuenta.domain.model.strategy.CuentaStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovimientoUseCaseImpl implements MovimientoUseCase {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    @Transactional
    public Movimiento registrarMovimiento(String numeroCuenta, Movimiento movimiento) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        BigDecimal nuevoSaldo;
        if (movimiento.getTipoMovimiento() == Movimiento.TipoMovimiento.DEPOSITO) {
            nuevoSaldo = cuenta.getSaldoDisponible().add(movimiento.getValor());
        } else {
            CuentaStrategy strategy = CuentaStrategyFactory.getStrategy(cuenta.getTipoCuenta());
            strategy.validarRetiro(cuenta.getSaldoDisponible(), movimiento.getValor());
            nuevoSaldo = cuenta.getSaldoDisponible().subtract(movimiento.getValor());
        }

        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);

        movimiento.setFecha(Instant.now());
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setCuentaId(cuenta.getId());
        return movimientoRepository.save(movimiento);
    }

    @Override
    public List<Movimiento> list() {
        return movimientoRepository.findAll();
    }

    @Override
    public Movimiento getById(UUID id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado"));
    }
}
